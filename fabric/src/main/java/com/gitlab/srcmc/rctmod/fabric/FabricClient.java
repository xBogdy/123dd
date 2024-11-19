package com.gitlab.srcmc.rctmod.fabric;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.platform.ModClient;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.neoforged.fml.config.ModConfig;

@Environment(EnvType.CLIENT)
public class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModClient.init();
        NeoForgeConfigRegistry.INSTANCE.register(ModCommon.MOD_ID, ModConfig.Type.CLIENT, RCTMod.get().getClientConfig().getSpec());
    }
}
