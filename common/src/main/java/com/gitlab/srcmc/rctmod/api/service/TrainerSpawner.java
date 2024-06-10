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
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TrainerSpawner {
    private static float KEY_TRAINER_SPAWN_WEIGHT_FACTOR = 60;

    private static class SpawnCandidate {
        public final String id;
        public final double weight;

        public SpawnCandidate(String id, double weight) {
            this.id = id;
            this.weight = weight;
        }
    }
    
    private Map<ResourceKey<Level>, Map<UUID, Set<String>>> spawnedFor = new HashMap<>();
    private Map<ResourceKey<Level>, Set<String>> spawnedTotal = new HashMap<>();

    public void registerMob(TrainerMob mob) {
        var originPlayer = mob.getOriginPlayer();
        var trainer = RCTMod.get().getTrainerManager().getData(mob).getTeam().getDisplayName();
        var dim = mob.level().dimension();
        this.getSpawnedTotal(dim).add(trainer);

        if(originPlayer != null) {
            this.getSpawnedFor(originPlayer, dim).add(trainer);
        }
    }

    public void unregisterMob(TrainerMob mob) {
        var trainer = RCTMod.get().getTrainerManager().getData(mob).getTeam().getDisplayName();
        this.getSpawnedTotal(mob.level().dimension()).remove(trainer);
        this.detachMobFromOrigin(mob);
    }

    public void detachMobFromOrigin(TrainerMob mob) {
        var originPlayer = mob.getOriginPlayer();
        
        if(originPlayer != null) {
            var trainer = RCTMod.get().getTrainerManager().getData(mob).getTeam().getDisplayName();
            var dim = mob.level().dimension();

            if(this.getSpawnedFor(originPlayer, dim).remove(trainer)) {
                var config = RCTMod.get().getServerConfig();

                if(config.logSpawning()) {
                    ModCommon.LOG.info(String.format("Detached Trainer: %s\n - Target Player: %s\n - Dimension: %s\n - Local Spawn Cap: %d/%d\n - Global Spawn Cap: %d/%d",
                        mob.getTrainerId(),
                        mob.level().getPlayerByUUID(originPlayer).getDisplayName().getString(),
                        this.getSpawnedFor(originPlayer, dim).size(),
                        config.maxTrainersPerPlayer(),
                        this.getSpawnedTotal(dim).size(),
                        config.maxTrainersTotal(),
                        dim.location().getPath()));
                }
            }
        }
    }

    public void attemptSpawnFor(Player player) {
        var config = RCTMod.get().getServerConfig();

        if(config.globalSpawnChance() < player.getRandom().nextFloat()) {
            return;
        }

        var dim = player.level().dimension();
        var spawnedTotal = this.getSpawnedTotal(dim);

        if(spawnedTotal.size() < config.maxTrainersTotal()) {
            var spawnedFor = this.getSpawnedFor(player.getUUID(), dim);

            if(spawnedFor.size() < config.maxTrainersPerPlayer()) {
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

    private Set<String> getSpawnedFor(UUID originPlayer, ResourceKey<Level> dim) {
        return this.spawnedFor
            .computeIfAbsent(dim, key -> new HashMap<>())
            .computeIfAbsent(originPlayer, key -> new HashSet<>());
    }

    private Set<String> getSpawnedTotal(ResourceKey<Level> dim) {
        return this.spawnedTotal.computeIfAbsent(dim, key -> new HashSet<>());
    }

    private void spawnFor(Player player, String trainerId, BlockPos pos) {
        var config = RCTMod.get().getServerConfig();
        var level = player.level();
        var mob = TrainerMob.getEntityType().create(level);
        mob.setPos(pos.getCenter().add(0, -0.5, 0));
        mob.setTrainerId(trainerId);
        mob.setOriginPlayer(player.getUUID());
        level.addFreshEntity(mob);

        if(config.logSpawning()) {
            var biome = level.getBiome(mob.blockPosition());
            var dim = level.dimension();

            ModCommon.LOG.info(String.format("Spawned Trainer: %s\n - Location: (%d, %d, %d)\n - Target Player: %s\n - Dimension: %s\n - Local Spawn Cap: %d/%d\n - Global Spawn Cap: %d/%d\n - Biome Tags:%s",
                mob.getTrainerId(),
                mob.blockPosition().getX(),
                mob.blockPosition().getY(),
                mob.blockPosition().getZ(),
                player.getDisplayName().getString(),
                dim.location().getPath(),
                this.getSpawnedFor(player.getUUID(), dim).size(),
                config.maxTrainersPerPlayer(),
                this.getSpawnedTotal(dim).size(),
                config.maxTrainersTotal(),
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

        // given tags without namespace match with from any namespace
        level.getBiome(pos).tags()
            .map(t -> t.location().getPath())
            .forEach(t -> tags.add(t));

        var config = RCTMod.get().getServerConfig();

        if(config.biomeTagBlacklist().stream().noneMatch(tags::contains)
        && (config.biomeTagWhitelist().isEmpty() || config.biomeTagWhitelist().stream().anyMatch(tags::contains))) {
            RCTMod.get().getTrainerManager().getAllData()
                .filter(e -> this.spawnedTotal.values().stream().noneMatch(set -> set.contains(e.getValue().getTeam().getDisplayName()))
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
