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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.gitlab.srcmc.rctmod.ModCommon;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class SavedMap<K, V, U> extends SavedData implements Map<K, V> {
    private Map<K, V> map = new HashMap<>();
    private Function<K, String> keyToTag;
    private Function<String, K> tagToKey;
    private Function<V, U> valuetoTag;
    private Function<U, V> tagToValue;
    private Function<CompoundTag, BiConsumer<String, U>> tagConsumer;
    private Function<CompoundTag, Function<String, U>> tagSupplier;

    public SavedMap(Function<K, String> keyToTag, Function<String, K> tagToKey, Function<V, U> valuetoTag, Function<U, V> tagToValue, Function<CompoundTag, BiConsumer<String, U>> tagConsumer, Function<CompoundTag, Function<String, U>> tagSupplier) {
        this.keyToTag = keyToTag;
        this.tagToKey = tagToKey;
        this.valuetoTag = valuetoTag;
        this.tagToValue = tagToValue;
        this.tagConsumer = tagConsumer;
        this.tagSupplier = tagSupplier;
    }

    public void load(CompoundTag tag) {
        this.clear();
        tag.getAllKeys().forEach(key -> this.put(this.tagToKey.apply(key), this.tagToValue.apply(this.tagSupplier.apply(tag).apply(key))));
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider provider) {
        for(var entry : this.map.entrySet()) {
            this.tagConsumer.apply(tag).accept(this.keyToTag.apply(entry.getKey()), this.valuetoTag.apply(entry.getValue()));
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
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(K key, V value) {
        var prev = this.map.put(key, value);

        if((prev == null && value != null) || (prev != null && !prev.equals(value))) {
            this.setDirty();
        }

        return prev;
    }

    @Override
    public V remove(Object key) {
        var removed = this.map.remove(key);

        if(removed != null) {
            this.setDirty();
        }

        return removed;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
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
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    public static String filePath(String context) {
        return String.format("%s.%s.map", ModCommon.MOD_ID, context);
    }
}
