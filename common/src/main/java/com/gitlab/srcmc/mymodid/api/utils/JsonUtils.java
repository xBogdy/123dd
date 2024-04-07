package com.gitlab.srcmc.mymodid.api.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;

public final class JsonUtils<T> {
    private JsonUtils() {}
    
    public static <T> T loadFromOrThrow(IoSupplier<InputStream> io, Class<T> type) {
        try(var rd = new BufferedReader(new InputStreamReader(io.get()))) {
            return new Gson().fromJson(rd, type);
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // deprecated?
    public static <T> T loadFromOrThrow(ResourceLocation location, Class<T> type) {
        try(var rd = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).openAsReader()) {
            return new Gson().fromJson(rd, type);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T loadFromOrThrow(IoSupplier<InputStream> io, TypeToken<T> type) {
        try(var rd = new BufferedReader(new InputStreamReader(io.get()))) {
            return new Gson().fromJson(rd, type);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    // deprecated?
    public static <T> T loadFromOrThrow(ResourceLocation location, TypeToken<T> type) {
        try(var rd = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).openAsReader()) {
            return new Gson().fromJson(rd, type);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String toJson(T obj) {
        return new Gson().toJson(obj);
    }
}
