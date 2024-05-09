package com.gitlab.srcmc.rctmod.client;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;

public class ModClient {
    private static ModClient instance;

    public static void init(ModClient instance) {
        ModClient.instance = instance;
    }
    
    public static ModClient get() {
        return instance == null ? (instance = new ModClient()) : instance;
    }

    public Optional<Player> getLocalPlayer() {
        return Optional.empty();
    }
}
