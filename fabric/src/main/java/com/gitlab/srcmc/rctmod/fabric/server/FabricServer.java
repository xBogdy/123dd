package com.gitlab.srcmc.rctmod.fabric.server;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.server.ModServer;

import net.fabricmc.api.DedicatedServerModInitializer;

public class FabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ModCommon.LOG.info("DEDICATED SERVER INIT");
        ModServer.init();
    }
}
