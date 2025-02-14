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
package com.gitlab.srcmc.rctmod.api.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.packs.resources.IoSupplier;

public final class JsonUtils<T> {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(TrainerType.Color.class, new TrainerType.Color.Serializer())
        .registerTypeAdapter(TrainerType.Color.class, new TrainerType.Color.Deserializer())
        .addSerializationExclusionStrategy(new AnnotationExclusionStrategy())
        .addDeserializationExclusionStrategy(new AnnotationExclusionStrategy())
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setLenient().create();

    public static <T> T loadFromOrThrow(String json, Class<T> type) {
        try(var rd = new BufferedReader(new StringReader(json))) {
            return GSON.fromJson(rd, type);
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T loadFromOrThrow(IoSupplier<InputStream> io, Class<T> type) {
        try(var rd = new BufferedReader(new InputStreamReader(io.get(), StandardCharsets.UTF_8))) {
            return GSON.fromJson(rd, type);
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T loadFromOrThrow(IoSupplier<InputStream> io, TypeToken<T> type) {
        try(var rd = new BufferedReader(new InputStreamReader(io.get(), StandardCharsets.UTF_8))) {
            return GSON.fromJson(rd, type);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String toJson(T obj) {
        return GSON.toJson(obj);
    }
    
    private JsonUtils() {
    }

    // see: https://www.baeldung.com/gson-exclude-fields-serialization#3-with-a-custom-annotation
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Exclude {}

    static class AnnotationExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    
        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            return field.getAnnotation(Exclude.class) != null;
        }
    }
}
