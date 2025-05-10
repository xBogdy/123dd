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
package com.gitlab.srcmc.rctmod.api.data.pack;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gitlab.srcmc.rctapi.api.util.Text;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.utils.LangKeys;
import com.gitlab.srcmc.rctmod.api.utils.JsonUtils.Exclude;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TrainerType implements Serializable {
    public static final long serialVersionUID = 0;
    public static final TrainerType DEFAULT = new TrainerType();

    private static final Map<String, TrainerType> REGISTRY = new HashMap<>();
    private static int counter;

    public static void register(String id, TrainerType trainerType) {
        trainerType.numericId = counter++;
        trainerType.id = id;
        
        if(REGISTRY.put(id, trainerType) != null) {
            ModCommon.LOG.error("duplicate trainer types '" + trainerType.id() + "'");
        }
    }

    public static TrainerType registerOrGet(String id, TrainerType trainerType) {
        var registered = REGISTRY.get(id);

        if(registered != null) {
            return registered;
        }

        REGISTRY.put(id, trainerType);
        return trainerType;
    }

    public static Collection<TrainerType> values() {
        return REGISTRY.values();
    }

    public static Set<String> ids() {
        return REGISTRY.keySet();
    }

    public static void clear() {
        REGISTRY.clear();
        counter = 0;
    }

    public static TrainerType valueOf(String id) {
        return REGISTRY.getOrDefault(id, DEFAULT);
    }

    private final Text name;
    private final String symbol;
    private final Color color;

    @Exclude
    private String id = "unknown";

    @Exclude
    private int numericId = -1;

    public TrainerType() {
        this(Text.translatable(LangKeys.TRAINER_TYPE_TITLE("unknown")));
    }

    public TrainerType(Text name) {
        this(name, new Color(0xffffff));
    }

    public TrainerType(Text name, Color color) {
        this(name, "", color);
    }

    public TrainerType(Text name, String symbol) {
        this(name, symbol, new Color(0xffffff));
    }

    public TrainerType(Text name, String symbol, Color color) {
        this.name = name;
        this.symbol = symbol;
        this.color = color;
    }

    public Text name() {
        return this.name;
    }

    public String symbol() {
        return this.symbol;
    }

    public String id() {
        return this.id;
    }

    public int color() {
        return this.color.rgb();
    }

    @Override
    public String toString() {
        return String.format("0x%02x:%s/%s", this.numericId, this.id, this.name);
    }

    @Override
    public int hashCode() {
        return this.numericId;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof TrainerType other) {
            return this.numericId == other.numericId;
        }

        return false;
    }

    public static record Color(int rgb) implements Serializable {
        private static final long serialVersionUID = 1;

        public static class Serializer implements JsonSerializer<Color> {
            @Override
            public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(String.format("%06x", src.rgb()));
            }
        }

        public static class Deserializer implements JsonDeserializer<Color> {
            @Override
            public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                try {
                    try {
                        return new Color(json.getAsInt());
                    } catch(NumberFormatException e) {
                        var s = json.getAsString().toLowerCase();

                        if(s.isEmpty()) {
                            s = "ffffff";
                        } else if(s.startsWith("0x")) {
                            s = s.substring(2);
                        }

                        return new Color(Integer.parseInt(s, 16));
                    }
                } catch(Exception e) {
                    throw new JsonParseException(e);
                }
            }
        }
    }
}
