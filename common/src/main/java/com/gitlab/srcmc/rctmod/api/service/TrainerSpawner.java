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
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class TrainerSpawner {
    private static float KEY_TRAINER_SPAWN_WEIGHT_FACTOR = 60;

    private class SpawnCandidate {
        public final String id;
        public final double weight;

        public SpawnCandidate(String id, double weight) {
            this.id = id;
            this.weight = weight;
        }
    }

    private Map<String, Integer> spawns = new HashMap<>();
    private Map<String, Integer> names = new HashMap<>();
    private Map<String, Integer> playerSpawns = new HashMap<>();
    
    private Map<String, Integer> persistentSpawns;
    private Map<String, Integer> persistentNames;
    private Map<String, Integer> persistentPlayerSpawns;

    private Set<TrainerMob> mobs = new HashSet<>();

    public void init(ServerLevel level) {
        this.spawns.clear();
        this.names.clear();
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

            if(mob.isRemoved()) {
                it.remove();
            } else if(!mob.level().isLoaded(mob.blockPosition())) {
                it.remove();
                mob.discard();
            }
        }
    }

    public void register(TrainerMob mob) {
        var spawns = mob.isPersistenceRequired() ? this.persistentSpawns : this.spawns;

        if(!spawns.containsKey(mob.getStringUUID())) {
            var name = RCTMod.get().getTrainerManager().getData(mob).getTeam().getDisplayName();
            var names = mob.isPersistenceRequired() ? this.persistentNames : this.names;
            var originPlayer = mob.getOriginPlayer();
            var playerSpawns = mob.isPersistenceRequired() ? this.persistentPlayerSpawns : this.playerSpawns;

            if(originPlayer != null) {
                playerSpawns.compute(originPlayer.toString(), (key, value) -> value == null ? 1 : value + 1);
            }

            names.compute(name, (key, value) -> value == null ? 1 : value + 1);
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
            var name = RCTMod.get().getTrainerManager().getData(mob).getTeam().getDisplayName();
            var names = mob.isPersistenceRequired() ? this.persistentNames : this.names;
            var originPlayer = mob.getOriginPlayer();
            var playerSpawns = mob.isPersistenceRequired() ? this.persistentPlayerSpawns : this.playerSpawns;

            if(originPlayer != null) {
                playerSpawns.compute(originPlayer.toString(), (key, value) -> value == 1 ? null : value - 1);
            }

            names.compute(name, (key, value) -> value == 1 ? null : value - 1);
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

            var name = RCTMod.get().getTrainerManager().getData(mob).getTeam().getDisplayName();
            var newName = RCTMod.get().getTrainerManager().getData(newTrainerId).getTeam().getDisplayName();
            var names = mob.isPersistenceRequired() ? this.persistentNames : this.names;
            names.compute(name, (key, value) -> value == 1 ? null : value - 1);
            names.compute(newName, (key, value) -> value == null ? 1 : value + 1);
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
            }

            if(newOriginPlayer != null) {
                playerSpawns.compute(newOriginPlayer.toString(), (key, value) -> value == null ? 1 : value + 1);
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
        return this.spawns.size() + this.persistentSpawns.size();
    }

    public int getSpawnCount(UUID playerId) {
        if(playerId != null) {
            return this.playerSpawns.getOrDefault(playerId.toString(), 0)
                + this.persistentPlayerSpawns.getOrDefault(playerId.toString(), 0);
        }

        return 0;
    }

    public void attemptSpawnFor(Player player) {
        var config = RCTMod.get().getServerConfig();

        if(config.globalSpawnChance() < player.getRandom().nextFloat()) {
            return;
        }

        if(this.getSpawnCount() < config.maxTrainersTotal()) {
            if(this.getSpawnCount(player.getUUID()) < config.maxTrainersPerPlayer()) {
                var pos = this.nextPos(player);
                
                if(pos != null) {
                    var spawnCandidate = this.nextSpawnCandidate(player, pos);
                    
                    if(spawnCandidate != null) {
                        this.spawnFor(player, spawnCandidate.id, pos);
                    }
                }
            }
        }
    }

    private boolean isUnique(String name) {
        return !this.names.containsKey(name) && !this.persistentNames.containsKey(name);
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
            var trainer = RCTMod.get().getTrainerManager().getData(mob).getTeam().getDisplayName();
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
        int air = 0, solid = 0;
        
        for(int i = dy; i != yEnd; i += yAdd) {
            var pos = new BlockPos(x, y + i, z);
            var bs = level.getBlockState(pos);

            if(bs.isValidSpawn(level, pos, TrainerMob.getEntityType())) {
                solid++;

                if(dy < 0) {
                    air = 0;
                }
            } else if(bs.isAir()) {
                air++;

                if(dy > 0) {
                    solid = 0;
                }
            } else {
                solid = 0;
                air = 0;
            }

            if(solid > 0 && air > 1) {
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
                .filter(e -> this.isUnique(e.getKey())
                    && e.getValue().getBiomeTagBlacklist().stream().noneMatch(tags::contains)
                    && (e.getValue().getBiomeTagWhitelist().isEmpty() || e.getValue().getBiomeTagWhitelist().stream().anyMatch(tags::contains)))
                .forEach(e -> {
                    var weight = this.computeWeight(player, e.getValue());

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

    private double computeWeight(Player player, TrainerMobData mobTr) {
        var config = RCTMod.get().getServerConfig();
        var tm = RCTMod.get().getTrainerManager();
        var playerTr = tm.getData(player);

        var mobLevel = mobTr.getTeam().getMembers().stream()
            .map(p -> p.getLevel())
            .max(Integer::compare).orElse(0);

        if(mobLevel <= playerTr.getLevelCap()) {
            var playerLevel = tm.getPlayerLevel(player);
            var keyTrainerFactor = 1f;

            if(mobTr.getRewardLevelCap() > playerTr.getLevelCap()) {
                var a = (10 - Math.min(9, playerTr.getLevelCap()/10))/2f;
                var b = Math.max(0, playerTr.getLevelCap() - playerLevel)*a + 1;
                keyTrainerFactor = KEY_TRAINER_SPAWN_WEIGHT_FACTOR/b;
            }

            int diff = Math.abs(Math.min(playerLevel, playerTr.getLevelCap()) - mobLevel);
            return diff > config.maxLevelDiff() ? 0 : ((config.maxLevelDiff() + 1) - diff)*mobTr.getSpawnWeightFactor()*keyTrainerFactor;
        }

        return 0;
    }
}
