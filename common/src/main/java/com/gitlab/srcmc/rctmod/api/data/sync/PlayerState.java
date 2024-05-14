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
package com.gitlab.srcmc.rctmod.api.data.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import net.minecraft.world.entity.player.Player;

public class PlayerState implements Serializable {
    public static final int SYNC_INTERVAL_TICKS = 60;
    private static final long serialVersionUID = 0;
    private static final Map<UUID, PlayerState> remoteStates = new HashMap<>();
    private static PlayerState localState;

    private Map<String, Integer> trainerDefeatCounts = new HashMap<>();
    private Map<TrainerMobData.Type, Integer> typeDefeatCounts = new HashMap<>();
    private int levelCap;

    private transient Player player;
    private transient PlayerState updated;
    private transient boolean hasChanges = true;

    public static PlayerState get(Player player) {
        var level = player.level();

        if(level.isClientSide) {
            if(player.isLocalPlayer()) {
                return localState == null ? (localState = new PlayerState(player)) : localState;
            }

            throw new IllegalArgumentException("Cannot retrieve player state of other players on this client");
        }

        return remoteStates.computeIfAbsent(player.getUUID(), uuid -> new PlayerState(player));
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

    public void addDefeat(String trainerId) {
        if(!this.trainerDefeatCounts.containsKey(trainerId)) {
            this.trainerDefeatCounts.put(trainerId, 1);
            this.updated.trainerDefeatCounts.put(trainerId, 1);
        } else {
            this.trainerDefeatCounts.compute(trainerId, (k, v) -> v + 1);
            this.updated.trainerDefeatCounts.put(trainerId, this.trainerDefeatCounts.get(trainerId));
        }

        var tm = RCTMod.get().getTrainerManager();
        var tt = tm.getData(trainerId).getType();

        this.typeDefeatCounts.compute(tt, (k, v) -> v == null ? 1 : v + 1);
        this.updated.typeDefeatCounts.put(tt, this.typeDefeatCounts.get(tt));
        this.hasChanges = true;
    }

    public int getTrainerDefeatCount(String trainerId) {
        var count = this.trainerDefeatCounts.get(trainerId);
        return count == null ? 0 : count;
    }

    public int getTypeDefeatCount(TrainerMobData.Type type) {
        var count = this.typeDefeatCounts.get(type);
        return count == null ? 0 : count;
    }

    public Map<String, Integer> getTrainerDefeatCounts() {
        return Collections.unmodifiableMap(this.trainerDefeatCounts);
    }

    public Map<TrainerMobData.Type, Integer> getTypeDefeatCounts() {
        return Collections.unmodifiableMap(this.typeDefeatCounts);
    }

    private PlayerState(Player player) {
        this.player = player;
        this.init();
        this.updated = this;
    }

    private PlayerState(PlayerState template) {
        this.levelCap = template.levelCap;
    }

    private void update(PlayerState updated) {
        this.trainerDefeatCounts.putAll(updated.trainerDefeatCounts);
        this.typeDefeatCounts.putAll(updated.typeDefeatCounts);
        this.levelCap = updated.levelCap;
    }

    private void init() {
        var level = player.level();

        if(!level.isClientSide) {
            var tm = RCTMod.get().getTrainerManager();
            var overworld = player.getServer().overworld();
            this.levelCap = tm.getData(player).getLevelCap();

            tm.getAllData().forEach(entry -> {
                var defCount = tm.getBattleMemory(overworld, entry.getKey()).getDefeatByCount(player);
                this.trainerDefeatCounts.put(entry.getKey(), defCount);

                if(defCount > 0) {
                    this.typeDefeatCounts.compute(entry.getValue().getType(), (k, v) -> v == null ? 1 : v + 1);
                }
            });
        }
    }
}
