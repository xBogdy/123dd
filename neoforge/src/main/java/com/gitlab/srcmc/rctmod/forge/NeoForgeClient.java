package com.gitlab.srcmc.rctmod.forge;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.platform.ModClient;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(value = ModCommon.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClient {
    public NeoForgeClient(ModContainer container) {
        ModClient.init();
        container.registerConfig(ModConfig.Type.CLIENT, RCTMod.get().getClientConfig().getSpec());
    }
}
