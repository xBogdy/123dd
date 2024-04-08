package com.gitlab.srcmc.mymodid.api.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.server.packs.resources.IoSupplier;

public final class JsonUtils<T> {
    private JsonUtils() {}

    private static final Gson GSON = new GsonBuilder()
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
}
