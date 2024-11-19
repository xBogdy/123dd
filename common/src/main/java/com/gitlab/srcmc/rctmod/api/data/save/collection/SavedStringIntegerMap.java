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
package com.gitlab.srcmc.rctmod.api.data.save.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.gitlab.srcmc.rctmod.ModCommon;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class SavedStringIntegerMap extends SavedData implements Map<String, Integer> {
    private Map<String, Integer> map = new HashMap<>();    

    public static SavedStringIntegerMap of(CompoundTag tag, Provider provider) {
        var map = new SavedStringIntegerMap();
        tag.getAllKeys().forEach(key -> map.put(key, tag.getInt(key)));
        return map;
    }

    public static String filePath(String context) {
        return String.format("%s.%s.map", ModCommon.MOD_ID, context);
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider provider) {
        for(var entry : this.map.entrySet()) {
            tag.putInt(entry.getKey(), entry.getValue());
        }

        return tag;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public Integer get(Object key) {
        return this.map.get(key);
    }

    @Override
    public Integer put(String key, Integer value) {
        var prev = this.map.put(key, value);

        if((prev == null && value != null) || (prev != null && !prev.equals(value))) {
            this.setDirty();
        }

        return prev;
    }

    @Override
    public Integer remove(Object key) {
        var removed = this.map.remove(key);

        if(removed != null) {
            this.setDirty();
        }

        return removed;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Integer> m) {
        this.map.putAll(m);
        this.setDirty();
    }

    @Override
    public void clear() {
        if(!this.map.isEmpty()) {
            this.map.clear();
            this.setDirty();
        }
    }

    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<Integer> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<String, Integer>> entrySet() {
        return this.map.entrySet();
    }
}
