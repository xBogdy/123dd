/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.save.collection.SavedStringIntegerMap;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class TrainerSpawner {
    private static float KEY_TRAINER_SPAWN_WEIGHT_FACTOR = 120;
    private static float UNDEFEATED_WEIGHT_FACTOR = 8;
    private static final int SPAWN_RETRIES = 4;
    private static final boolean CAN_SPAWN_IN_WATER = false; // experimental

    private class SpawnCandidate {
        public final String id;
        public final double weight;

        public SpawnCandidate(String id, double weight) {
            this.id = id;
            this.weight = weight;
        }
    }

    private Map<String, Integer> spawns = new HashMap<>();
    private Map<String, Integer> identities = new HashMap<>();
    private Map<String, Integer> playerSpawns = new HashMap<>();
    
    private Map<String, Integer> persistentSpawns;
    private Map<String, Integer> persistentNames;
    private Map<String, Integer> persistentPlayerSpawns;

    private Set<TrainerMob> mobs = new HashSet<>();
    private Map<UUID, Integer> tracked = new HashMap<>();

    public void init(ServerLevel level) {
        this.spawns.clear();
        this.identities.clear();
        this.playerSpawns.clear();
        this.mobs.clear();
        
        this.persistentSpawns = level.getDataStorage().computeIfAbsent(
            SavedStringIntegerMap::of,
            SavedStringIntegerMap::new,
            SavedStringIntegerMap.filePath("spawn.uuids"));

        this.persistentNames = level.getDataStorage().computeIfAbsent(
            SavedStringIntegerMap::of,
            SavedStringIntegerMap::new,
            SavedStringIntegerMap.filePath("spawn.names"));

        this.persistentPlayerSpawns = level.getDataStorage().computeIfAbsent(
            SavedStringIntegerMap::of,
            SavedStringIntegerMap::new,
            SavedStringIntegerMap.filePath("spawn.counts"));

        if(RCTMod.get().getServerConfig().logSpawning()) {
            ModCommon.LOG.info("Initialized trainer spawner" + this.persistentSpawns.keySet().stream().reduce("", (u1, u2) -> u1 + " " + u2));
        }
    }

    public void checkDespawns() {
        var it = this.mobs.iterator();

        while(it.hasNext()) {
            var mob = it.next();

            if(mob.isRemoved() || mob.isPersistenceRequired()) {
                it.remove();
            } else if(mob.shouldDespawn()) {
                it.remove();
                mob.discard();
            }
        }
    }

    public void register(TrainerMob mob) {
        var spawns = mob.isPersistenceRequired() ? this.persistentSpawns : this.spawns;

        if(!spawns.containsKey(mob.getStringUUID())) {
            var identity = RCTMod.get().getTrainerManager().getData(mob).getTeam().getIdentity();
            var identities = mob.isPersistenceRequired() ? this.persistentNames : this.identities;
            var originPlayer = mob.getOriginPlayer();
            var playerSpawns = mob.isPersistenceRequired() ? this.persistentPlayerSpawns : this.playerSpawns;

            if(originPlayer != null) {
                playerSpawns.compute(originPlayer.toString(), (key, value) -> value == null ? 1 : value + 1);
                this.track(mob, originPlayer);
            }

            identities.compute(identity, (key, value) -> value == null ? 1 : value + 1);
            spawns.put(mob.getStringUUID(), 0);

            if(!mob.isPersistenceRequired()) {
                this.mobs.add(mob);
            }

            var config = RCTMod.get().getServerConfig();

            if(config.logSpawning()) {
                ModCommon.LOG.info(String.format("Registered%strainer '%s' (%s) to spawner, attached to %s (%d/%d), (%d/%d)",
                    mob.isPersistenceRequired() ? " persistent " : " ",
                    mob.getTrainerId(), mob.getStringUUID(), originPlayer,
                    this.getSpawnCount(originPlayer), config.maxTrainersPerPlayer(),
                    this.getSpawnCount(), config.maxTrainersTotal()));
            }
        }
    }

    public void unregister(TrainerMob mob) {
        var spawns = mob.isPersistenceRequired() ? this.persistentSpawns : this.spawns;

        if(spawns.containsKey(mob.getStringUUID())) {
            var identity = RCTMod.get().getTrainerManager().getData(mob).getTeam().getIdentity();
            var identities = mob.isPersistenceRequired() ? this.persistentNames : this.identities;
            var originPlayer = mob.getOriginPlayer();
            var playerSpawns = mob.isPersistenceRequired() ? this.persistentPlayerSpawns : this.playerSpawns;

            if(originPlayer != null) {
                playerSpawns.compute(originPlayer.toString(), (key, value) -> value == 1 ? null : value - 1);
                this.untrack(mob, originPlayer);
            }

            identities.compute(identity, (key, value) -> value == 1 ? null : value - 1);
            spawns.remove(mob.getStringUUID());

            var config = RCTMod.get().getServerConfig();

            if(config.logSpawning()) {
                ModCommon.LOG.info(String.format("Unregistered%strainer '%s' (%s) from spawner, attached to %s (%d/%d), (%d/%d)",
                    mob.isPersistenceRequired() ? " persistent " : " ",
                    mob.getTrainerId(), mob.getStringUUID(), originPlayer,
                    this.getSpawnCount(originPlayer), config.maxTrainersPerPlayer(),
                    this.getSpawnCount(), config.maxTrainersTotal()));
            }
        }
    }

    public void unregisterPersistent(String mobUUID) {
        this.persistentNames.remove(mobUUID);
        this.persistentSpawns.remove(mobUUID);
        this.persistentPlayerSpawns.remove(mobUUID);
    }

    public boolean isRegistered(TrainerMob mob) {
        return this.spawns.containsKey(mob.getStringUUID()) || this.persistentSpawns.containsKey(mob.getStringUUID());
    }

    public void notifyChangeTrainerId(TrainerMob mob, String newTrainerId) {
        var spawns = mob.isPersistenceRequired() ? this.persistentSpawns : this.spawns;

        if(spawns.containsKey(mob.getStringUUID())) {
            ModCommon.LOG.info(String.format("Changing trainer id '%s' -> '%s' (%s)", mob.getTrainerId(), newTrainerId, mob.getStringUUID()));

            var identity = RCTMod.get().getTrainerManager().getData(mob).getTeam().getIdentity();
            var newIdentity = RCTMod.get().getTrainerManager().getData(newTrainerId).getTeam().getIdentity();
            var identities = mob.isPersistenceRequired() ? this.persistentNames : this.identities;
            identities.compute(identity, (key, value) -> value == 1 ? null : value - 1);
            identities.compute(newIdentity, (key, value) -> value == null ? 1 : value + 1);
        }
    }

    public void notifyChangeOriginPlayer(TrainerMob mob, UUID newOriginPlayer) {
        var spawns = mob.isPersistenceRequired() ? this.persistentSpawns : this.spawns;

        if(spawns.containsKey(mob.getStringUUID())) {
            ModCommon.LOG.info(String.format("Changing origin player '%s' -> '%s' (%s)", mob.getOriginPlayer(), newOriginPlayer, mob.getStringUUID()));

            var originPlayer = mob.getOriginPlayer();
            var playerSpawns = mob.isPersistenceRequired() ? this.persistentPlayerSpawns : this.playerSpawns;

            if(originPlayer != null) {
                playerSpawns.compute(originPlayer.toString(), (key, value) -> value == 1 ? null : value - 1);
                this.untrack(mob, originPlayer);
            }

            if(newOriginPlayer != null) {
                playerSpawns.compute(newOriginPlayer.toString(), (key, value) -> value == null ? 1 : value + 1);
                this.track(mob, newOriginPlayer);
            }
        }
    }

    public void notifyChangePersistence(TrainerMob mob, boolean newPersistence) {
        if(this.spawns.containsKey(mob.getStringUUID())) {
            this.unregister(mob);
            mob.setPersistent(newPersistence);
            this.register(mob);
        }
    }

    public int getSpawnCount() {
        return this.spawns.size();
    }

    public int getSpawnCount(UUID playerId) {
        if(playerId != null) {
            return this.playerSpawns.getOrDefault(playerId.toString(), 0);
        }

        return 0;
    }

    public void attemptSpawnFor(Player player) {
        var config = RCTMod.get().getServerConfig();

        if(config.globalSpawnChance() < player.getRandom().nextFloat() || RCTMod.get().getTrainerManager().getPlayerLevel(player) == 0) {
            return;
        }

        if(this.getSpawnCount() < config.maxTrainersTotal()) {
            if(this.getSpawnCount(player.getUUID()) < config.maxTrainersPerPlayer()) {
                for(int i = 0; i < SPAWN_RETRIES; i++) {
                    var pos = this.nextPos(player);

                    if(pos != null) {
                        var spawnCandidate = this.nextSpawnCandidate(player, pos);
                        
                        if(spawnCandidate != null) {
                            this.spawnFor(player, spawnCandidate.id, pos);
                        }

                        break;
                    }
                }
            }
        }
    }

    private boolean isUnique(String identity) {
        return !this.identities.containsKey(identity) && !this.persistentNames.containsKey(identity);
    }

    private void spawnFor(Player player, String trainerId, BlockPos pos) {
        var config = RCTMod.get().getServerConfig();
        var level = player.level();
        var mob = TrainerMob.getEntityType().create(level);
        mob.setPos(pos.getCenter().add(0, -0.5, 0));
        mob.setTrainerId(trainerId);
        mob.setOriginPlayer(player.getUUID());
        level.addFreshEntity(mob);
        this.register(mob);

        if(config.logSpawning()) {
            var trainer = RCTMod.get().getTrainerManager().getData(trainerId).getTeam().getDisplayName();
            var biome = level.getBiome(mob.blockPosition());
            var dim = level.dimension();

            ModCommon.LOG.info(String.format("Spawned trainer '%s' (%s) at (%d, %d, %d), %s:%s",
                trainer, mob.getTrainerId(),
                mob.blockPosition().getX(),
                mob.blockPosition().getY(),
                mob.blockPosition().getZ(),
                dim.location().getPath(),
                biome.tags().map(t -> t.location().getPath()).reduce("", (t1, t2) -> t1 + " " + t2)));
        }
    }

    @SuppressWarnings("unused")
    private BlockPos nextPos(Player player) {
        var config = RCTMod.get().getServerConfig();
        var level = player.level();
        var rng = player.getRandom();
        
        int d = config.maxHorizontalDistanceToPlayers() - config.minHorizontalDistanceToPlayers();
        int dx = (config.minHorizontalDistanceToPlayers() + (Math.abs(rng.nextInt()) % d)) * (rng.nextInt() < 0 ? -1 : 1);
        int dz = (config.minHorizontalDistanceToPlayers() + (Math.abs(rng.nextInt()) % d)) * (rng.nextInt() < 0 ? -1 : 1);
        int dy = rng.nextInt() > 0 ? config.maxVerticalDistanceToPlayers() : -config.maxVerticalDistanceToPlayers();

        int x = player.getBlockX() + dx;
        int z = player.getBlockZ() + dz;
        int y = player.getBlockY();

        int yEnd = dy > 0 ? -(dy + 1) : -(dy - 1);
        int yAdd = dy > 0 ? -1 : 1;
        
        int prevState = -1; // 0: air, 1: water, 2: snow, 3: solid
        int validCount = 0;
        
        for(int i = dy; i != yEnd; i += yAdd) {
            var pos = new BlockPos(x, y + i, z);
            var bs = level.getBlockState(pos);

            if(bs.is(Blocks.SNOW)) {
                if(dy < 0) {
                    if(prevState == 3) {
                        validCount = 1;
                    } else {
                        validCount = 0;
                    }
                } else {
                    if(prevState == 0) {
                        validCount++;
                    } else {
                        validCount = 0;
                    }
                }

                prevState = 2;
            } else if(bs.isFaceSturdy(level, pos, Direction.UP)) {
                if(dy < 0) {
                    validCount = 1;
                } else {
                    if(prevState == 0 || prevState == 1) {
                        validCount++;
                    } else {
                        validCount = 0;
                    }
                }

                prevState = 3;
            } else if(bs.isAir()) {
                if(dy < 0) {
                    if(validCount > 0) {
                        validCount++;
                    }
                } else {
                    validCount = Math.min(2, validCount + 1);
                }

                prevState = 0;
            } else if(CAN_SPAWN_IN_WATER && bs.getFluidState().is(Fluids.WATER)) {
                if(dy < 0) {
                    if(validCount > 0) {
                        validCount++;
                    }
                } else {
                    validCount = Math.min(2, validCount + 1);
                }

                prevState = 1;
            } else {
                prevState = -1;
                validCount = 0;
            }

            if(validCount > 2) {
                return dy < 0 ? pos.below() : pos.above();
            }
        }

        return null;
    }

    private SpawnCandidate nextSpawnCandidate(Player player, BlockPos pos) {
        var candidates = new ArrayList<SpawnCandidate>();
        var level = player.level();
        var tags = level.getBiome(pos).tags()
            .map(t -> t.location().getNamespace() + ":" + t.location().getPath())
            .collect(Collectors.toSet());

        // given tags without namespace match with any namespace
        level.getBiome(pos).tags()
            .map(t -> t.location().getPath())
            .forEach(t -> tags.add(t));

        var config = RCTMod.get().getServerConfig();

        if(config.biomeTagBlacklist().stream().noneMatch(tags::contains)
        && (config.biomeTagWhitelist().isEmpty() || config.biomeTagWhitelist().stream().anyMatch(tags::contains))) {
            RCTMod.get().getTrainerManager().getAllData()
                .filter(e -> this.isUnique(e.getValue().getTeam().getIdentity())
                    && e.getValue().getBiomeTagBlacklist().stream().noneMatch(tags::contains)
                    && (e.getValue().getBiomeTagWhitelist().isEmpty() || e.getValue().getBiomeTagWhitelist().stream().anyMatch(tags::contains)))
                .forEach(e -> {
                    var weight = this.computeWeight(player, e.getKey(), e.getValue());

                    if(weight > 0) {
                        candidates.add(new SpawnCandidate(e.getKey(), weight));
                    }
                });
        }

        return candidates.size() > 0 ? this.selectRandom(player.getRandom(), candidates) : null;
    }

    // based of: https://stackoverflow.com/a/6737362
    private SpawnCandidate selectRandom(RandomSource rng, List<SpawnCandidate> candidates) {
        var totalWeight = candidates
            .stream().map(c -> c.weight)
            .reduce(0D, (a, b) -> a + b);
        
        int i = 0;

        for(var r =  rng.nextDouble()*totalWeight; i < candidates.size() - 1; ++i) {
            r -= candidates.get(i).weight;
            if(r <= 0.0) break;
        }
        
        return candidates.get(i);
    }

    private double computeWeight(Player player, String trainerId, TrainerMobData mobTr) {
        var config = RCTMod.get().getServerConfig();
        var tm = RCTMod.get().getTrainerManager();
        var playerTr = tm.getData(player);
        var reqLevelCap = mobTr.getRequiredLevelCap();

        if(reqLevelCap <= playerTr.getLevelCap()) {
            var ps = PlayerState.get(player);

            for(var type : TrainerMobData.Type.values()) {
                if(ps.getTypeDefeatCount(type) < mobTr.getRequiredDefeats(type)) {
                    return 0;
                }
            }

            var playerLevel = tm.getPlayerLevel(player);
            var keyTrainerFactor = 1f;

            if(mobTr.getRewardLevelCap() > playerTr.getLevelCap()
            || ((mobTr.getType() == TrainerMobData.Type.LEADER
                || mobTr.getType() == TrainerMobData.Type.E4
                || mobTr.getType() == TrainerMobData.Type.CHAMP
                || mobTr.getType() == TrainerMobData.Type.BOSS)
                && tm.getBattleMemory((ServerLevel)player.level(), trainerId).getDefeatByCount(player) == 0)
            ) {
                var a = (10 - Math.min(9, playerTr.getLevelCap()/10))/2f;
                var b = Math.max(0, playerTr.getLevelCap() - playerLevel)*a + 1;
                keyTrainerFactor = KEY_TRAINER_SPAWN_WEIGHT_FACTOR/b;
            }

            var undefeatedFactor = ps.getTrainerDefeatCount(trainerId) == 0 ? UNDEFEATED_WEIGHT_FACTOR : 1f;
            int diff = Math.abs(Math.min(playerLevel, playerTr.getLevelCap()) - reqLevelCap);
            return diff > config.maxLevelDiff() ? 0 : ((config.maxLevelDiff() + 1) - diff)*mobTr.getSpawnWeightFactor()*undefeatedFactor*keyTrainerFactor;
        }

        return 0;
    }

    private void track(TrainerMob trainer, UUID playerUUID) {
        if(playerUUID != null) {
            var player = trainer.level().getPlayerByUUID(playerUUID);

            if(player != null) {
                var tm = RCTMod.get().getTrainerManager();
                var trMob = tm.getData(trainer);
                
                if(trMob.getRewardLevelCap() > tm.getData(player).getLevelCap()) {
                    PlayerState.get(player).setTarget(trainer.getId());
                    this.tracked.put(playerUUID, trainer.getId());
                }
            }
        }
    }
    
    private void untrack(TrainerMob trainer, UUID playerUUID) {
        var id = this.tracked.get(playerUUID);

        if(id != null && trainer.getId() == id) {
            var player = trainer.level().getPlayerByUUID(playerUUID);
            this.tracked.remove(playerUUID);

            if(player != null) {
                PlayerState.get(player).setTarget(-1);
            }
        }
    }
}
