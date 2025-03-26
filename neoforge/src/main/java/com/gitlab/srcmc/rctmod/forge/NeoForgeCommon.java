/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.forge;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.commands.arguments.TokenArgumentType;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ModCommon.MOD_ID)
public class NeoForgeCommon {
    public NeoForgeCommon(ModContainer container) {
        ModRegistries.init();
        container.registerConfig(ModConfig.Type.SERVER, RCTMod.getInstance().getServerConfig().getSpec());
        container.getEventBus().addListener(NeoForgeCommon::onCommonSetup);
        container.getEventBus().addListener(NeoForgeCommon::onConfigLoading);
        container.getEventBus().addListener(NeoForgeCommon::onConfigReloading);
    }

    static void onCommonSetup(FMLCommonSetupEvent event) {
        ArgumentTypeInfos.registerByClass(TokenArgumentType.class, ModRegistries.ArgumentTypes.TOKEN_ARGUMENT_TYPE.get());
        ModCommon.init();
    }

    static void onConfigLoading(ModConfigEvent.Loading event) {
        onConfigLoadingOrReloading(event.getConfig());
    }

    static void onConfigReloading(ModConfigEvent.Reloading event) {
        onConfigLoadingOrReloading(event.getConfig());
    }

    static void onConfigLoadingOrReloading(ModConfig config) {
        switch (config.getType()) {
            case SERVER:
                RCTMod.getInstance().getServerConfig().reload();
                RCTMod.getInstance().getTrainerManager().setReloadRequired();
                break;
            case CLIENT:
                RCTMod.getInstance().getClientConfig().reload();
                break;
            default:
                break;
        }
    }
}
