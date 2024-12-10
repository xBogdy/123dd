package com.gitlab.srcmc.rctmod.server;

import com.gitlab.srcmc.rctmod.network.PlayerStatePayload;

import dev.architectury.networking.NetworkManager;

public class ModServer {
    public static void init() {
        NetworkManager.registerS2CPayloadType(PlayerStatePayload.TYPE, PlayerStatePayload.CODEC);
    }
}
