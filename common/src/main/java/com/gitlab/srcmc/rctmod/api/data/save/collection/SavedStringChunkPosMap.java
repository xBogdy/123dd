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
package com.gitlab.srcmc.rctmod.api.data.save.collection;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public class SavedStringChunkPosMap extends SavedMap<String, ChunkPos, int[]> {
    public static SavedStringChunkPosMap of(CompoundTag tag, Provider provider) {
        var map = new SavedStringChunkPosMap();
        map.load(tag);
        return map;
    }

    public SavedStringChunkPosMap() {
        super(k -> k, t -> t, v -> new int[]{v.x, v.z}, t -> new ChunkPos(t[0], t[1]), ct -> ct::putIntArray, ct -> ct::getIntArray);
    }
}
