package com.gitlab.srcmc.rctmod.fabric;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.platform.ModServer;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;

public class FabricCommon implements ModInitializer {
    @Override
    public void onInitialize() {
        ModCommon.init();
        ModServer.init();
        NeoForgeConfigRegistry.INSTANCE.register(ModCommon.MOD_ID, ModConfig.Type.SERVER, RCTMod.get().getServerConfig().getSpec());
    }
}
