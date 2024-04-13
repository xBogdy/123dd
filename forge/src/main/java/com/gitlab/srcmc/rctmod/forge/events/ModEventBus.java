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

import java.util.List;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.pokemon.ExperienceGainedPreEvent;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.advancements.criteria.DefeatCountTrigger;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.forge.ModRegistries;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import kotlin.Unit;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBus {
    @SubscribeEvent
    static void onModConstruct(FMLConstructModEvent event) {
        RCTMod.init(ModRegistries.LootItemConditions.LEVEL_RANGE);
    }

    @SubscribeEvent
    static void onRegisterClientReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RCTMod.get().getClientDataManager());
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, ModEventBus::handleBattleVictory);
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.HIGHEST, ModEventBus::handleExperienceGained);
        event.enqueueWork(() -> CriteriaTriggers.register(DefeatCountTrigger.get()));
    }

    @SubscribeEvent
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(ModRegistries.Entities.TRAINER.get(), TrainerMob.createAttributes().build());
    }

    private static Unit handleBattleVictory(BattleVictoryEvent event) {
        if(!checkForTrainerBattle(event.getWinners(), true)) {
            checkForTrainerBattle(event.getLosers(), false);
        }

        return Unit.INSTANCE;
    }

    private static Unit handleExperienceGained(ExperienceGainedPreEvent event) {
        var owner = event.getPokemon().getOwnerPlayer();

        if(owner != null) {
            var playerTr = RCTMod.get().getTrainerManager().getData(owner);
            var maxExp = event.getPokemon().getExperienceToLevel(playerTr.getLevelCap());
            event.setExperience(Math.min(event.getExperience(), maxExp));
        }

        return Unit.INSTANCE;
    }

    private static boolean checkForTrainerBattle(List<BattleActor> actors, boolean winners) {
        for(var actor : actors) {
            var trainerBattle = RCTMod.get().getTrainerManager().getBattle(actor.getUuid());

            if(trainerBattle.isPresent()) {
                RCTMod.get().getTrainerManager().removeBattle(actor.getUuid());
                trainerBattle.get().distributeRewards(winners);
                return true;
            }
        }

        return false;
    }
}
