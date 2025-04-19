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
package com.gitlab.srcmc.rctmod.api.data;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;

// TODO: maybe buffer translatable component and listen for "language change" event
public class Text implements Serializable, Comparable<Text> {
    private static final long serialVersionUID = 0L;
    private static Gson GSON = new Gson();
    private String literal, translatable;

    public Text setLiteral(String literal) {
        this.literal = literal;
        return this;
    }

    public Text setTranslatable(String translatable) {
        this.translatable = translatable;
        return this;
    }

    public Component asComponent() {
        return this.translatable != null
            ? Component.translatable(this.translatable)
            : this.literal != null
                ? Component.literal(this.literal)
                : Component.empty();
    }

    public static class Deserializer implements JsonDeserializer<Text> {
        @Override
        public Text deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                var t = json.getAsString();
                return new Text().setLiteral(t);
            } catch(UnsupportedOperationException e) {
                return GSON.fromJson(json, typeOfT);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.literal, this.translatable);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Text o) && Objects.equals(this.literal, o.literal) && Objects.equals(this.translatable, o.translatable);
    }

    @Override
    public int compareTo(Text o) {
        return this.asComponent().getString().compareTo(o.asComponent().getString());
    }
}
