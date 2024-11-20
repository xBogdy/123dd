package com.gitlab.srcmc.rctmod.forge;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(ModCommon.MOD_ID)
public class NeoForgeCommon {
    public NeoForgeCommon(ModContainer container) {
        ModCommon.init();
        container.registerConfig(ModConfig.Type.SERVER, RCTMod.getInstance().getServerConfig().getSpec());
    }
}
