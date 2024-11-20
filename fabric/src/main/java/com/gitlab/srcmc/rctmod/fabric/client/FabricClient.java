package com.gitlab.srcmc.rctmod.fabric.client;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.client.ModClient;

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
        NeoForgeConfigRegistry.INSTANCE.register(ModCommon.MOD_ID, ModConfig.Type.CLIENT, RCTMod.getInstance().getClientConfig().getSpec());
    }
}
