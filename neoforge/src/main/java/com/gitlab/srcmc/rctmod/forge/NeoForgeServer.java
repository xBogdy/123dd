package com.gitlab.srcmc.rctmod.forge;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.platform.ModServer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = ModCommon.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class NeoForgeServer {
    public NeoForgeServer(IEventBus bus) {
        ModServer.init();
    }
}
