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

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class TrainerPlayerData extends SavedData {
    private int levelCap;

    public static TrainerPlayerData of(CompoundTag tag, Provider provider) {
        var tpd = new TrainerPlayerData();
        tpd.levelCap = tag.getInt("levelCap");
        return tpd;
    }

    public TrainerPlayerData() {
        var cfg = RCTMod.get().getServerConfig();
        this.levelCap = Math.max(0, Math.min(100, cfg.initialLevelCap() + cfg.bonusLevelCap()));
    }

    public static String filePath(Player player) {
        return String.format("%s.player.%s.stat", ModCommon.MOD_ID, player.getUUID().toString());
    }

    public int getLevelCap() {
        return this.levelCap;
    }

    public void setLevelCap(Player player, int levelCap) {
        if(this.levelCap != levelCap) {
            PlayerState.get(player).setLevelCap(levelCap);
            this.levelCap = levelCap;
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, Provider provider) {
        compoundTag.putInt("levelCap", this.levelCap);
        return compoundTag;
    }
}
