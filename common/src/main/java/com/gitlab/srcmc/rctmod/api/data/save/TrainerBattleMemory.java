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
package com.gitlab.srcmc.rctmod.api.data.save;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class TrainerBattleMemory extends SavedData {
    public static final int VERSION = 0;
    
    private Map<UUID, Integer> defeatedBy = new HashMap<>();

    public static TrainerBattleMemory of(CompoundTag tag, Provider provider) {
        var tbm = new TrainerBattleMemory();
        tag.getAllKeys().forEach(key -> tbm.defeatedBy.put(UUID.fromString(key), tag.getInt(key)));
        return tbm;
    }

    public static String filePath(String trainerId) {
        return String.format("%s.trainers.%s.mem", ModCommon.MOD_ID, trainerId);
    }

    public void addDefeatedBy(String trainerId, Player player) {
        var count = this.defeatedBy.get(player.getUUID());

        if(count == null) {
            count = 0;
        }

        if(count < Integer.MAX_VALUE) {
            var rct = RCTMod.getInstance();

            if(rct.getSeriesManager().getGraph(rct.getTrainerManager().getData(player).getCurrentSeries()).contains(trainerId)) {
                PlayerState.get(player).addDefeat(trainerId);
            }

            this.defeatedBy.put(player.getUUID(), count + 1);
            this.setDirty();
        }
    }

    public void setDefeatedBy(String trainerId, Player player, int count) {
        var prevCount = this.defeatedBy.put(player.getUUID(), count);

        if(prevCount == null || prevCount != count) {
            var rct = RCTMod.getInstance();

            if(rct.getSeriesManager().getGraph(rct.getTrainerManager().getData(player).getCurrentSeries()).contains(trainerId)) {
                PlayerState.get(player).setDefeats(trainerId, count);
            }

            this.setDirty();
        }
    }

    public int getDefeatByCount(Player player) {
        var count = this.defeatedBy.get(player.getUUID());
        return count == null ? 0 : count;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, Provider provider) {
        this.defeatedBy.forEach((uuid, count) -> compoundTag.putInt(uuid.toString(), count));
        return compoundTag;
    }
}
