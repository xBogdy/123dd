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
package com.gitlab.srcmc.rctmod.fabric;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.ModRegistries;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.commands.arguments.TokenArgumentType;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.neoforged.fml.config.ModConfig;

public class FabricCommon implements ModInitializer {
    public FabricCommon() {
        ModRegistries.init(true);
    }

    @Override
    public void onInitialize() {
        ArgumentTypeRegistry.registerArgumentType(
            ModRegistries.location("token_argument_type"),
            TokenArgumentType.class,
            SingletonArgumentInfo.contextFree(TokenArgumentType::token));

        ModCommon.init();
        NeoForgeConfigRegistry.INSTANCE.register(ModCommon.MOD_ID, ModConfig.Type.SERVER, RCTMod.getInstance().getServerConfig().getSpec());
        NeoForgeModConfigEvents.loading(ModCommon.MOD_ID).register(FabricCommon::onConfigLoadingOrReloading);
        NeoForgeModConfigEvents.reloading(ModCommon.MOD_ID).register(FabricCommon::onConfigLoadingOrReloading);
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
