/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
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
import com.gitlab.srcmc.rctmod.api.data.save.collection.SavedMap;
import com.gitlab.srcmc.rctmod.api.data.save.collection.SavedStringChunkPosMap;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.SavedData.Factory;

public class TrainerSpawner {
    private static float KEY_TRAINER_SPAWN_WEIGHT_FACTOR = 64;
    private static float NON_KEY_TRAINER_SPAWN_CHANCE_DIV = 4;
    private static final int SPAWN_RETRIES = 8;
    private static final boolean CAN_SPAWN_IN_WATER = false; // experimental
    private static final double TRAINER_DIRECT_SPAWN_CHANCE = 0.42;

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
    private Map<String, ChunkPos> persistentChunks;

    private Set<TrainerMob> mobs = new HashSet<>();
    private Set<TrainerMob> persistentMobs = new HashSet<>();

    public void init(ServerLevel level) {
        this.spawns.clear();
        this.identities.clear();
        this.playerSpawns.clear();
        this.persistentMobs.clear();
        this.mobs.clear();

        this.persistentChunks = level.getDataStorage().computeIfAbsent(
            new Factory<>(SavedStringChunkPosMap::new, SavedStringChunkPosMap::of, DataFixTypes.LEVEL),
            SavedMap.filePath("spawn.chunks"));

        var server = level.getServer();

        this.persistentChunks.values().forEach(cp -> {
            // TODO: only force update the chunk in the level the trainer was actually in
            server.getAllLevels().forEach(l -> l.getChunkSource().updateChunkForced(cp, true));
        });

        if(RCTMod.getInstance().getServerConfig().logSpawning()) {
            ModCommon.LOG.info("Initialized Trainer Spawner service");
        }
    }

    public Set<TrainerMob> getSpawns() {
        return Sets.union(this.mobs, this.persistentMobs);
    }

    public void checkDespawns() {
        var it = this.mobs.iterator();

        while(it.hasNext()) {
            var mob = it.next();

            if(mob.isRemoved() || mob.isPersistenceRequired()) {
                it.remove();
            } else if(mob.shouldDespawn()) {
                mob.remove(RemovalReason.UNLOADED_TO_CHUNK);
                it.remove();
            }
        }
    }

    public void register(TrainerMob mob) {
        var identity = RCTMod.getInstance().getTrainerManager().getData(mob).getTrainerTeam().getIdentity();

        if(mob.isPersistenceRequired()) {
            this.persistentChunks.put(mob.getStringUUID(), mob.chunkPosition());
            this.persistentMobs.add(mob);
        }

        if(!this.spawns.containsKey(mob.getStringUUID())) {
            var originPlayer = mob.getOriginPlayer();

            if(originPlayer != null) {
                this.playerSpawns.compute(originPlayer.toString(), (key, value) -> value == null ? 1 : value + 1);
            }

            this.identities.compute(identity, (key, value) -> value == null ? 1 : value + 1);
            this.spawns.put(mob.getStringUUID(), 0);

            if(!mob.isPersistenceRequired()) {
                this.mobs.add(mob);
            }

            var config = RCTMod.getInstance().getServerConfig();

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
        if(this.spawns.containsKey(mob.getStringUUID())) {
            var identity = RCTMod.getInstance().getTrainerManager().getData(mob).getTrainerTeam().getIdentity();
            var originPlayer = mob.getOriginPlayer();

            if(originPlayer != null) {
                this.playerSpawns.compute(originPlayer.toString(), (key, value) -> value == null || value <= 1 ? null : value - 1);
            }

            this.identities.compute(identity, (key, value) -> value == null || value <= 1 ? null : value - 1);
            this.spawns.remove(mob.getStringUUID());

            if(mob.isPersistenceRequired()) {
                this.persistentChunks.remove(mob.getStringUUID());
                this.persistentMobs.remove(mob);
            }

            var config = RCTMod.getInstance().getServerConfig();

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
        for(var m : this.persistentMobs) {
            if(m.getStringUUID().equals(mobUUID)) {
                m.setPersistent(false);
                return;
            }
        }
    }

    public boolean isRegistered(TrainerMob mob) {
        return this.spawns.containsKey(mob.getStringUUID());
    }

    public void notifyChangeTrainerId(TrainerMob mob, String newTrainerId) {
        if(this.spawns.containsKey(mob.getStringUUID())) {
            ModCommon.LOG.info(String.format("Changing trainer id '%s' -> '%s' (%s)", mob.getTrainerId(), newTrainerId, mob.getStringUUID()));
            var identity = RCTMod.getInstance().getTrainerManager().getData(mob).getTrainerTeam().getIdentity();
            var newIdentity = RCTMod.getInstance().getTrainerManager().getData(newTrainerId).getTrainerTeam().getIdentity();
            this.identities.compute(identity, (key, value) -> value == null || value <= 1 ? null : value - 1);
            this.identities.compute(newIdentity, (key, value) -> value == null ? 1 : value + 1);
        }
    }

    public void notifyChangeOriginPlayer(TrainerMob mob, UUID newOriginPlayer) {
        if(this.spawns.containsKey(mob.getStringUUID())) {
            var originPlayer = mob.getOriginPlayer();

            if(originPlayer != null) {
                this.playerSpawns.compute(originPlayer.toString(), (key, value) -> value == null || value <= 1 ? null : value - 1);
            }

            if(newOriginPlayer != null) {
                this.playerSpawns.compute(newOriginPlayer.toString(), (key, value) -> value == null ? 1 : value + 1);
            }

            if(RCTMod.getInstance().getServerConfig().logSpawning()) {
                ModCommon.LOG.info(String.format("Changed origin player for '%s': '%s' -> '%s'", mob.getTrainerId(), String.valueOf(originPlayer), String.valueOf(newOriginPlayer)));
            }
        }
    }

    public void notifyChangePersistence(TrainerMob mob, boolean newPersistence) {
        if(this.spawns.containsKey(mob.getStringUUID()) ) {
            this.unregister(mob);
            mob.setPersistent(newPersistence, true);
            this.register(mob);
        }
    }

    public int getSpawnCount() {
        return this.getSpawnCount(false);
    }

    public int getSpawnCount(boolean includePersistent) {
        return this.spawns.size() - (includePersistent ? 0 : this.persistentMobs.size());
    }

    public int getSpawnCount(UUID playerId) {
        if(playerId != null) {
            return this.playerSpawns.getOrDefault(playerId.toString(), 0);
        }

        return 0;
    }

    public boolean attemptSpawnFor(Player player, String trainerId, BlockPos pos) {
        return this.attemptSpawnFor(player, trainerId, pos, false, false);
    }

    public boolean attemptSpawnFor(Player player, String trainerId, BlockPos pos, boolean setHome, boolean noOrigin) {
        var cfg = RCTMod.getInstance().getServerConfig();
        return this.attemptSpawnFor(player, trainerId, pos, setHome, noOrigin, false, cfg.globalSpawnChance(), cfg.globalSpawnChanceMinimum());
    }

    public boolean attemptSpawnFor(Player player, String trainerId, BlockPos pos, boolean setHome, boolean noOrigin, boolean guaruantee, double globalChance, double globalChanceMin) {
        var level = player.level();

        if(RCTMod.getInstance().getTrainerManager().isValidId(trainerId) && TrainerSpawner.canSpawnAt(level, pos) && this.canSpawnFor(player, noOrigin, globalChance, globalChanceMin)) {
            var tmd = RCTMod.getInstance().getTrainerManager().getData(trainerId);

            if(tmd != null && this.isUnique(tmd.getTrainerTeam().getIdentity())) {
                if(guaruantee || this.computeChance(player, trainerId, tmd) >= player.getRandom().nextDouble()) {
                    this.spawnFor(player, trainerId, pos, setHome, noOrigin);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean attemptSpawnFor(Player player) {
        var cfg = RCTMod.getInstance().getServerConfig();

        if(this.canSpawnFor(player, false, cfg.globalSpawnChance(), cfg.globalSpawnChanceMinimum())) {
            for(int i = 0; i < SPAWN_RETRIES; i++) {
                var pos = this.nextPos(player);

                if(pos != null) {
                    var spawnCandidate = this.nextSpawnCandidate(player, pos);
                    
                    if(spawnCandidate != null) {
                        this.spawnFor(player, spawnCandidate.id, pos);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private static boolean canSpawnAt(Level level, BlockPos blockPos) {
        return !level.getBlockState(blockPos.below()).isAir()
            && level.getBlockState(blockPos).isAir()
            && level.getBlockState(blockPos.above()).isAir();
    }

    private boolean isUnique(String identity) {
        return !this.identities.containsKey(identity);
    }

    private boolean canSpawnFor(Player player, boolean noOrigin, double globalChance, double globalChanceMin) {
        var config = RCTMod.getInstance().getServerConfig();
        var spawnCountPl = this.getSpawnCount(player.getUUID());
        var maxCountPl = config.maxTrainersPerPlayer();
        var chanceRange = Math.max(0, globalChance - globalChanceMin);

        return this.getSpawnCount() < config.maxTrainersTotal()
            && RCTMod.getInstance().getTrainerManager().getPlayerLevel(player) > 0
            && (noOrigin || (spawnCountPl < maxCountPl
                && globalChance - chanceRange*(maxCountPl > 1 ? Math.min(1, spawnCountPl/(double)maxCountPl) : 1) >= player.getRandom().nextFloat()));
    }

    private void spawnFor(Player player, String trainerId, BlockPos pos) {
        this.spawnFor(player, trainerId, pos, false, false);
    }

    private void spawnFor(Player player, String trainerId, BlockPos pos, boolean setHome, boolean noOrigin) {
        var config = RCTMod.getInstance().getServerConfig();
        var level = player.level();
        var mob = TrainerMob.getEntityType().create(level);
        mob.setPos(pos.getCenter().add(0, -0.5, 0));
        mob.setTrainerId(trainerId);

        if(!noOrigin) {
            mob.setOriginPlayer(player.getUUID());
        }

        level.addFreshEntity(mob);
        this.register(mob);

        if(setHome) {
            mob.setHomePos(pos);
        }

        if(config.logSpawning()) {
            var trainer = RCTMod.getInstance().getTrainerManager().getData(trainerId).getTrainerTeam().getName();
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
    public BlockPos nextPos(Player player) {
        var config = RCTMod.getInstance().getServerConfig();
        var level = player.level();
        var rng = player.getRandom();
        
        int d = config.maxHorizontalDistanceToPlayers() - config.minHorizontalDistanceToPlayers();
        int dx = (config.minHorizontalDistanceToPlayers() + (Math.abs(rng.nextInt()) % d)) * (rng.nextBoolean() ? -1 : 1);
        int dz = (config.minHorizontalDistanceToPlayers() + (Math.abs(rng.nextInt()) % d)) * (rng.nextBoolean() ? -1 : 1);
        int dy = rng.nextBoolean() ? config.maxVerticalDistanceToPlayers() : -config.maxVerticalDistanceToPlayers();

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

        var config = RCTMod.getInstance().getServerConfig();

        // flags to skip dimension processing
        var dimensionBlacklisted = config.dimensionBlacklist().contains(level.dimension().location().toString());
        var dimensionWhitelisted = config.dimensionWhitelist().isEmpty() || config.dimensionWhitelist().contains(level.dimension().location().toString());

        if(!dimensionBlacklisted && dimensionWhitelisted
            && config.biomeTagBlacklist().stream().noneMatch(tags::contains)
            && (config.biomeTagWhitelist().isEmpty() || config.biomeTagWhitelist().stream().anyMatch(tags::contains))) {

            RCTMod.getInstance().getTrainerManager().getAllData(PlayerState.get(player).getCurrentSeries())
                .filter(e -> this.isUnique(e.getValue().getTrainerTeam().getIdentity())
                    && e.getValue().getBiomeTagBlacklist().stream().noneMatch(tags::contains)
                    && (e.getValue().getBiomeTagWhitelist().isEmpty() || e.getValue().getBiomeTagWhitelist().stream().anyMatch(tags::contains)))
                .forEach(e -> {
                    var weight = this.computeWeight(player, e.getKey(), e.getValue());

                    if (weight > 0) {
                        candidates.add(new SpawnCandidate(e.getKey(), weight));
                    }
                });
        }

        // no candidates if dimension/biome blacklisted or not whitelisted
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

    private double computeChance(Player player, String trainerId, TrainerMobData mobTr) {
        var ps = PlayerState.get(player);
        var config = RCTMod.getInstance().getServerConfig();
        var tm = RCTMod.getInstance().getTrainerManager();
        var playerLevel = tm.getPlayerLevel(player);
        var reqLevelCap = mobTr.getRequiredLevelCap();
        var levelCap = ps.getLevelCap();
        var chance = TRAINER_DIRECT_SPAWN_CHANCE;

        if(!ps.isKeyTrainer(trainerId)) {
            chance /= NON_KEY_TRAINER_SPAWN_CHANCE_DIV;
        }

        var e = (1.0  - Math.min(config.maxLevelDiff(), Math.abs(Math.min(playerLevel, levelCap) - reqLevelCap))/(double)config.maxLevelDiff());
        return chance * e * e;
    }

    private double computeWeight(Player player, String trainerId, TrainerMobData mobTr) {
        var ps = PlayerState.get(player);

        if(!ps.canBattle(trainerId)) {
            return 0;
        }

        var config = RCTMod.getInstance().getServerConfig();
        var tm = RCTMod.getInstance().getTrainerManager();
        var playerLevel = tm.getPlayerLevel(player);
        var reqLevelCap = mobTr.getRequiredLevelCap();
        var levelCap = ps.getLevelCap();
        var keyTrainerFactor = 1f;
        var isKey = ps.isKeyTrainer(trainerId);

        if(isKey) {
            var a = (10 - Math.min(9, levelCap/10))/2f;
            var b = Math.max(0, reqLevelCap - playerLevel)*a + 1;
            keyTrainerFactor = KEY_TRAINER_SPAWN_WEIGHT_FACTOR/b;
        }

        int diff = Math.abs(Math.min(playerLevel, levelCap) - reqLevelCap);
        return diff > config.maxLevelDiff() ? 0 : ((config.maxLevelDiff() + 1) - diff)*mobTr.getSpawnWeightFactor()*keyTrainerFactor;
    }
}
