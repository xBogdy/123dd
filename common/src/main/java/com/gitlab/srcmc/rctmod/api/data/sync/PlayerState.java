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
package com.gitlab.srcmc.rctmod.api.data.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.include.com.google.common.base.Objects;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.service.SeriesManager.SeriesGraph;

import net.minecraft.world.entity.player.Player;

public class PlayerState implements Serializable {
    public static final int SYNC_INTERVAL_TICKS = 5;
    public static final int MIN_BATCH_SIZE = 64;
    public static final int MAX_BATCH_SIZE = 512;

    private static final long serialVersionUID = 0;
    private static final Map<UUID, PlayerState> remoteStates = new HashMap<>();
    private static PlayerState localState;

    private Map<String, Integer> trainerDefeatCounts = new HashMap<>();
    private Map<TrainerType, Integer> typeDefeatCounts = new HashMap<>();
    private Set<String> defeatedTrainerIds = new HashSet<>();
    private Set<String> removedDefeatedTrainerIds = new HashSet<>();
    private String currentSeries = "";
    private int levelCap;

    private transient Player player;
    private transient PlayerState updated;
    private transient boolean hasChanges = true;
    private transient Map<TrainerType, Integer> distinctTypeDefeatCounts = new HashMap<>();
    private transient Map<String, Boolean> keyTrainerMap = new HashMap<>();
    private transient SeriesGraph nextGraph;

    public static void initFor(Player player) {
        if(player.level().isClientSide) {
            if(!player.isLocalPlayer()) {
                throw new IllegalArgumentException("Cannot initialize player state of other players on this client");
            }

            PlayerState.localState = new PlayerState(player);
        } else {
            PlayerState.remoteStates.put(player.getUUID(), new PlayerState(player));
            RCTMod.getInstance().getTrainerManager().getData(player).sync();
        }
    }

    public static PlayerState get(Player player) {
        if(player.level().isClientSide) {
            if(!player.isLocalPlayer()) {
                throw new IllegalArgumentException("Cannot retrieve player state of other players on this client");
            }

            return PlayerState.localState;
        }

        return PlayerState.remoteStates.get(player.getUUID());
    }

    public byte[] serializeUpdate() {
        if(!this.hasChanges) {
            return new byte[]{};
        }

        var buf = new ByteArrayOutputStream();

        try(var oos = new ObjectOutputStream(buf)) {
            oos.writeObject(this.updated);
            this.updated = new PlayerState(this);
            this.hasChanges = false;
            return buf.toByteArray();
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void deserializeUpdate(byte[] bytes) {
        var buf = new ByteArrayInputStream(bytes);

        try(var ois = new ObjectInputStream(buf)) {
            this.update((PlayerState)ois.readObject());
            this.hasChanges = false;
        } catch(IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setLevelCap(int levelCap) {
        if(this.levelCap != levelCap) {
            this.levelCap = levelCap;
            this.updated.levelCap = levelCap;
            this.hasChanges = true;
        }
    }

    public int getLevelCap() {
        return this.levelCap;
    }

    public void setCurrentSeries(String seriesId) {
        if(!Objects.equal(this.currentSeries, seriesId)) {
            this.currentSeries = seriesId;
            this.updated.currentSeries = seriesId;
            this.keyTrainerMap.clear();
            this.updateDefeatCounts();
            this.nextGraph = null;
            this.hasChanges = true;
        }
    }

    public String getCurrentSeries() {
        return this.currentSeries;
    }

    public void addProgressDefeat(String trainerId) {
        if(this.defeatedTrainerIds.add(trainerId)) {
            this.updated.defeatedTrainerIds.add(trainerId);
            this.updated.removedDefeatedTrainerIds.remove(trainerId);
            this.keyTrainerMap.clear();
            this.nextGraph = null;
            this.hasChanges = true;
        }
    }

    public void removeProgressDefeat(String trainerId) {
        if(this.defeatedTrainerIds.remove(trainerId)) {
            this.updated.removedDefeatedTrainerIds.add(trainerId);
            this.updated.defeatedTrainerIds.remove(trainerId);
            this.keyTrainerMap.clear();
            this.nextGraph = null;
            this.hasChanges = true;
        }
    }

    public void removeProgressDefeats() {
        if(this.defeatedTrainerIds.size() > 0) {
            this.updated.removedDefeatedTrainerIds.addAll(this.defeatedTrainerIds);
            this.updated.defeatedTrainerIds.clear();
            this.defeatedTrainerIds.clear();
            this.keyTrainerMap.clear();
            this.nextGraph = null;
            this.hasChanges = true;
        }
    }

    public void addDefeat(String trainerId) {
        var tm = RCTMod.getInstance().getTrainerManager();
        var tt = tm.getData(trainerId).getType();

        if(!this.trainerDefeatCounts.containsKey(trainerId)) {
            this.trainerDefeatCounts.put(trainerId, 1);
            this.updated.trainerDefeatCounts.put(trainerId, 1);
            this.distinctTypeDefeatCounts.compute(tt, (k, v) -> v == null ? 1 : v + 1);
        } else {
            this.updated.trainerDefeatCounts.put(trainerId, this.trainerDefeatCounts.compute(trainerId, (k, v) -> v == Integer.MAX_VALUE ? v : v + 1));
        }

        this.updated.typeDefeatCounts.put(tt, this.typeDefeatCounts.compute(tt, (k, v) -> v == null ? 1 : v == Integer.MAX_VALUE ? v : v + 1));
        this.keyTrainerMap.clear();
        this.nextGraph = null;
        this.hasChanges = true;
    }

    public void setDefeats(String trainerId, int defeats) {
        var currentDefeats = this.trainerDefeatCounts.getOrDefault(trainerId, 0);

        if(defeats != currentDefeats) {
            var tm = RCTMod.getInstance().getTrainerManager();
            var tt = tm.getData(trainerId).getType();
            var typeDefeats = this.typeDefeatCounts.getOrDefault(tt, 0);
            var newTypeDefeats = typeDefeats + (defeats - currentDefeats);
            
            if(newTypeDefeats == 0) {
                this.typeDefeatCounts.remove(tt);
            } else {
                this.typeDefeatCounts.put(tt, newTypeDefeats);
            }

            if(defeats == 0) {
                this.trainerDefeatCounts.remove(trainerId);
                this.distinctTypeDefeatCounts.compute(tt, (k, v) -> v - 1);
            } else {
                this.trainerDefeatCounts.put(trainerId, defeats);

                if(currentDefeats == 0) {
                    this.distinctTypeDefeatCounts.compute(tt, (k, v) -> v == null ? 1 : v + 1);
                }
            }

            this.updated.trainerDefeatCounts.put(trainerId, defeats);
            this.updated.typeDefeatCounts.put(tt, newTypeDefeats);
            this.keyTrainerMap.clear();
            this.nextGraph = null;
            this.hasChanges = true;
        }
    }

    public long getTypeDefeatCount(TrainerType type) {
        return this.getTypeDefeatCount(type, false);
    }

    public long getTypeDefeatCount(TrainerType type, boolean distinct) {
        var count = distinct ? this.distinctTypeDefeatCounts.get(type) : this.typeDefeatCounts.get(type);
        return count == null ? 0 : count;
    }

    public int getTrainerDefeatCount(String trainerId) {
        var count = this.trainerDefeatCounts.get(trainerId);
        return count == null ? 0 : count;
    }

    public long getTrainerDefeatCount() {
        return this.getTrainerDefeatCount(false);
    }

    public long getTrainerDefeatCount(boolean distinct) {
        if(distinct) {
            return this.trainerDefeatCounts.size();
        } else {
            return this.typeDefeatCounts.values().stream().mapToLong(i -> i).reduce(0, (i, j) -> i + j);
        }
    }
    
    public boolean isKeyTrainer(String trainerId) {
        if(this.nextGraph == null) {
            this.nextGraph = RCTMod.getInstance().getSeriesManager()
                .getGraph(this.currentSeries)
                .getNext(this.defeatedTrainerIds);
        }

        return this.keyTrainerMap.computeIfAbsent(trainerId, k -> this.nextGraph.contains(trainerId));
    }

    public boolean canBattle(String trainerId) {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(trainerId);

        return this.getLevelCap() >= tmd.getRequiredLevelCap()
            && tmd.getMissingRequirements(this.defeatedTrainerIds).findFirst().isEmpty();
    }

    protected Map<String, Integer> getTrainerDefeatCounts() {
        return Collections.unmodifiableMap(this.trainerDefeatCounts);
    }

    protected Map<TrainerType, Integer> getTypeDefeatCounts() {
        return Collections.unmodifiableMap(this.typeDefeatCounts);
    }

    private PlayerState(Player player) {
        this.player = player;
        this.updated = this;

        if(!player.level().isClientSide) {
            this.updateDefeatCounts();
        }
    }

    private PlayerState(PlayerState template) {
        this.levelCap = template.levelCap;
        this.currentSeries = template.currentSeries;
    }

    private void update(PlayerState updated) {
        var tm = RCTMod.getInstance().getTrainerManager();

        updated.trainerDefeatCounts.forEach((trainerId, count) -> {
            var tt = tm.getData(trainerId).getType();

            if(count == 0) {
                if(this.trainerDefeatCounts.remove(trainerId) != null) {
                    this.distinctTypeDefeatCounts.compute(tt, (k, v) -> v > 1 ? v - 1 : null);
                }
            } else {
                if(this.trainerDefeatCounts.put(trainerId, count) == null) {
                    this.distinctTypeDefeatCounts.compute(tt, (k, v) -> v == null ? 1 : v + 1);
                }
            }
        });

        updated.typeDefeatCounts.forEach((type, count) -> {
            if(count == 0) {
                this.typeDefeatCounts.remove(type);
            } else {
                this.typeDefeatCounts.put(type, count);
            }
        });

        updated.defeatedTrainerIds.forEach(this.defeatedTrainerIds::add);
        updated.removedDefeatedTrainerIds.forEach(this.defeatedTrainerIds::remove);
        this.levelCap = updated.levelCap;
        this.currentSeries = updated.currentSeries;
        this.keyTrainerMap.clear();
        this.nextGraph = null;
    }

    private void updateDefeatCounts() {
        var tm = RCTMod.getInstance().getTrainerManager();
        var overworld = this.player.getServer().overworld();
        List.copyOf(this.trainerDefeatCounts.keySet()).forEach(tid -> this.setDefeats(tid, 0));
        tm.getAllData(this.getCurrentSeries()).forEach(entry -> this.setDefeats(entry.getKey(), tm.getBattleMemory(overworld, entry.getKey()).getDefeatByCount(this.player)));
    }
}
