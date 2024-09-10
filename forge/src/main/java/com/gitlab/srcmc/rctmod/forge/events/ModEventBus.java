/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctmod.forge.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.advancements.criteria.DefeatCountTrigger;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.config.ClientConfig;
import com.gitlab.srcmc.rctmod.config.ServerConfig;
import com.gitlab.srcmc.rctmod.forge.CobblemonHandler;
import com.gitlab.srcmc.rctmod.forge.ModRegistries;
import com.gitlab.srcmc.rctmod.forge.api.service.Configs;
import com.gitlab.srcmc.rctmod.forge.api.service.LootConditions;
import com.gitlab.srcmc.rctmod.forge.api.service.PlayerController;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBus {
    @SubscribeEvent
    static void onModConstruct(FMLConstructModEvent event) {
        var serverConfig = new ServerConfig();
        var clientConfig = new ClientConfig();
        var mlx = ModLoadingContext.get();

        mlx.registerConfig(serverConfig.getType(), serverConfig.getSpec());
        mlx.registerConfig(clientConfig.getType(), clientConfig.getSpec());
        RCTMod.init(new PlayerController(), new LootConditions(), new Configs(clientConfig, serverConfig, null));
    }

    @SubscribeEvent
    static void onRegisterClientReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RCTMod.get().getClientDataManager());
        event.registerReloadListener(RCTMod.get().getTrainerManager());
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, CobblemonHandler::handleBattleVictory);
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.HIGHEST, CobblemonHandler::handleExperienceGained);
        event.enqueueWork(() -> CriteriaTriggers.register(DefeatCountTrigger.get()));
    }

    @SubscribeEvent
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(ModRegistries.Entities.TRAINER.get(), TrainerMob.createAttributes().build());
    }
}
