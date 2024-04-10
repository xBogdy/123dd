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

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class TrainerSpawner {
    private static final int MIN_DISTANCE_TO_PLAYERS = 30;
    public static final int MAX_DISTANCE_TO_PLAYERS = 80;
    private static final int MAX_VERTICAL_DISTAINCE_TO_PLAYERS = 30;
    private static final int MAX_TRAINERS_PER_PLAYER = 3;
    private static final int MAX_TRAINERS_TOTAL = 20;
    private static final int MAX_LEVEL_DIFF = 1; // TODO: mod config
    private static final int SPAWN_TICK_COOLDOWN = 20;
    private static final int SPAWN_ATTEMPS = 3;

    private static class SpawnCandidate {
        public final String id, name;
        public final double weight;

        public SpawnCandidate(String id, String name, double weight) {
            this.id = id;
            this.name = name;
            this.weight = weight;
        }
    }
    
    private Map<UUID, Set<String>> spawnedFor = new HashMap<>();
    private Set<String> spawnedTotal = new HashSet<>();

    public void registerMob(TrainerMob mob) {
        ModCommon.LOG.info("REGISTERING: " + mob.getDisplayName().getString());
        var originPlayer = mob.getOriginPlayer();
        var tmd = RCTMod.get().getTrainerManager().getData(mob);
        this.spawnedTotal.add(tmd.getTeam().getDisplayName());

        if(originPlayer != null) {
            var spawnedFor = this.spawnedFor.get(originPlayer);

            if(spawnedFor == null) {
                spawnedFor = new HashSet<>();
                this.spawnedFor.put(originPlayer, spawnedFor);
            }

            spawnedFor.add(tmd.getTeam().getDisplayName());
        }
    }

    public void unregisterMob(TrainerMob mob) {
        ModCommon.LOG.info("UNREGISTERING: " + mob.getDisplayName().getString());
        var originPlayer = mob.getOriginPlayer();
        var tmd = RCTMod.get().getTrainerManager().getData(mob);
        this.spawnedTotal.remove(tmd.getTeam().getDisplayName());

        if(originPlayer != null) {
            var spawnedFor = this.spawnedFor.get(originPlayer);

            if(spawnedFor != null) {
                spawnedFor.remove(tmd.getTeam().getDisplayName());
            }
        }
    }

    public void attemptSpawnFor(Player player) {
        ModCommon.LOG.info("SPAWN ATTEMPT FOR: " + player.getName().getString());

        if(this.spawnedTotal.size() < MAX_TRAINERS_TOTAL) {
            var spawnedFor = this.spawnedFor.get(player.getUUID());

            if(spawnedFor == null) {
                spawnedFor = new HashSet<>();
                this.spawnedFor.put(player.getUUID(), spawnedFor);
            }

            if(spawnedFor.size() < MAX_TRAINERS_PER_PLAYER) {
                var pos = this.nextPos(player);
                
                if(pos != null) {
                    // var level = player.level();
                    // var biome = level.getBiome(pos); // TODO: biome/dimension spawn restrictions
                    // var dimension = level.get
                    var spawnCandidate = this.nextSpawnCandidate(player);

                    if(spawnCandidate != null) {
                        this.spawnFor(player, spawnCandidate.id, spawnCandidate.name, pos);
                    }
                }
            }
        }
    }

    private void spawnFor(Player player, String trainerId, String trainerName, BlockPos pos) {
        var level = player.level();
        var mob = TrainerMob.getEntityType().create(level);
        mob.setPos(pos.getCenter());
        mob.setTrainerId(trainerId);
        mob.setOriginPlayer(player.getUUID());
        level.addFreshEntity(mob);
        ModCommon.LOG.info("SPAWNED TRAINER: " + mob.getDisplayName().getString());
    }

    private BlockPos nextPos(Player player) {
        var level = player.level();
        var rng = player.getRandom();
        int d = MAX_DISTANCE_TO_PLAYERS - MIN_DISTANCE_TO_PLAYERS;
        int dx = (MIN_DISTANCE_TO_PLAYERS + (d % rng.nextInt())) * (rng.nextInt() % 2 == 0 ? 1 : -1);
        int dz = (MIN_DISTANCE_TO_PLAYERS + (d % rng.nextInt())) * (rng.nextInt() % 2 == 0 ? 1 : -1);
        int dy = MAX_VERTICAL_DISTAINCE_TO_PLAYERS;

        int x = player.getBlockX() + dx;
        int z = player.getBlockZ() + dz;
        int y = player.getBlockY();
        int air = 0;
        
        for(int i = dy; i >= -dy; i--) {
            var pos = new BlockPos(x, y + i, z);
            ModCommon.LOG.info("POSSIBLE POS: " + pos.toShortString() + ", air: " + level.getBlockState(pos).isAir());

            if(level.getBlockState(pos).isAir()) {
                air++;
            } else {
                if(air > 1) {
                    return pos.above();
                } else {
                    air = 0;
                }
            }
        }

        return null;
    }

    private SpawnCandidate nextSpawnCandidate(Player player) {
        var candidates = new ArrayList<SpawnCandidate>();

        RCTMod.get().getTrainerManager().getAllData()
            .filter(e -> !this.spawnedTotal.contains(e.getValue().getTeam().getDisplayName()))
            .forEach(e -> {
                var weight = this.computeWeight(player, e.getValue());

                if(weight > 0) {
                    candidates.add(new SpawnCandidate(e.getKey(),
                        e.getValue().getTeam().getDisplayName(),
                        weight));
                }
            });

        ModCommon.LOG.info("SPAWN CANDIDATES: " + candidates.size());
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
        var playerTr = RCTMod.get()
            .getTrainerManager()
            .getData(player);

        int diff = Math.abs(mobTr.getTeam()
            .getMembers().stream().map(p -> p.getLevel())
            .max(Integer::compare).orElse(0) - playerTr.getLevelCap());

        return diff <= MAX_LEVEL_DIFF ? ((MAX_LEVEL_DIFF + 1) - diff)*mobTr.getSpawnChance() : 0;
    }
}
