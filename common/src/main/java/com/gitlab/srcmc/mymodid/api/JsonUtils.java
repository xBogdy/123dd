package com.gitlab.srcmc.mymodid.api;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public final class JsonUtils<T> {
    private JsonUtils() {}
    
    public static <T> T loadFromOrThrow(ResourceLocation location, Class<T> type) {
        try(var rd = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).openAsReader()) {
            return new Gson().fromJson(rd, type);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
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
