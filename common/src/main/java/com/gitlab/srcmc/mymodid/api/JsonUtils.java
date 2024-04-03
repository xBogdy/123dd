package com.gitlab.srcmc.mymodid.api;

import java.io.IOException;

import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public abstract class JsonUtils<T> {
    public static <T> T loadFromOrThrow(ResourceLocation location, Class<T> type) {
        try(var rd = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).openAsReader()) {
            return new Gson().fromJson(rd, type);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
