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
import net.minecraft.resources.ResourceLocation;

public class SavedDimensionBlockPosIntegerMap extends SavedMap<ResourceLocation, SavedBlockPosIntegerMap, CompoundTag> {
    public static SavedDimensionBlockPosIntegerMap of(CompoundTag tag, Provider provider) {
        var map = new SavedDimensionBlockPosIntegerMap();
        map.load(tag);
        return map;
    }

    public SavedDimensionBlockPosIntegerMap() {
        super(k -> k.toString(), ResourceLocation::parse,
            v -> v.save(new CompoundTag(), null),
            t -> { var m = new SavedBlockPosIntegerMap(); m.load(t); return m; },
            t -> t::put, t -> t::getCompound);
    }
}
