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
package com.gitlab.srcmc.rctmod.api.data.save;

import java.util.HashMap;
import java.util.Map;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData.Type;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class TrainerPlayerData extends SavedData {
    private int levelCap;

    // TODO: deprecated (info can be retrieved with PlayerState)
    private Map<Type, Integer> defeats = new HashMap<>();

    public static TrainerPlayerData of(CompoundTag tag) {
        var tpd = new TrainerPlayerData();
        tpd.levelCap = tag.getInt("levelCap");
        
        var defeatedTag = tag.getCompound("defeats");
        tpd.defeats.clear();

        for(var key : defeatedTag.getAllKeys()) {
            tpd.defeats.put(Type.valueOf(key), defeatedTag.getInt(key));
        }
        
        return tpd;
    }

    public TrainerPlayerData() {
        this.levelCap = RCTMod.get().getServerConfig().initialLevelCap();
    }

    public static String filePath(Player player) {
        return String.format("%s.player.%s.stat", ModCommon.MOD_ID, player.getUUID().toString());
    }

    public int getLevelCap() {
        return this.levelCap;
    }
    
    public int getDefeats(Type type) {
        return this.defeats.getOrDefault(type, 0);
    }

    public void setLevelCap(Player player, int levelCap) {
        if(this.levelCap != levelCap) {
            PlayerState.get(player).setLevelCap(levelCap);
            this.levelCap = levelCap;
            setDirty();
        }
    }

    public void addDefeat(Type type) {
        this.addDefeat(type, 1);
    }

    public void addDefeat(Type type, int count) {
        var prevCount = this.getDefeats(type);

        this.setDefeats(type, count < 0
            ? prevCount > -count ? prevCount + count : 0
            : Integer.MAX_VALUE - count > prevCount ? prevCount + count : Integer.MAX_VALUE);
    }

    public void setDefeats(Type type, int count) {
        var prev = this.defeats.put(type, Math.max(0, count));

        if(prev == null || prev != count) {
            this.setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("levelCap", this.levelCap);
        var defeatedTag = new CompoundTag();
        
        for(var e : defeats.entrySet()) {
            defeatedTag.putInt(e.getKey().name(), e.getValue());
        }
        
        compoundTag.put("defeats", defeatedTag);
        return compoundTag;
    }
}
