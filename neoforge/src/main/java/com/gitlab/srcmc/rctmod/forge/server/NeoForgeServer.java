package com.gitlab.srcmc.rctmod.forge.server;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.server.ModServer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = ModCommon.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class NeoForgeServer {
    public NeoForgeServer(ModContainer container) {
        ModCommon.LOG.info("DEDICATED SERVER INIT");
        ModServer.init();
    }
}
