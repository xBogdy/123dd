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
package com.gitlab.srcmc.rctmod.fabric;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.advancements.criteria.DefeatCountTrigger;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.commands.PlayerCommands;
import com.gitlab.srcmc.rctmod.commands.TrainerCommands;
import com.gitlab.srcmc.rctmod.config.ClientConfig;
import com.gitlab.srcmc.rctmod.config.ServerConfig;
import com.gitlab.srcmc.rctmod.fabric.api.services.Configs;
import com.gitlab.srcmc.rctmod.fabric.api.services.LootConditions;
import com.gitlab.srcmc.rctmod.fabric.api.services.PlayerController;
import com.gitlab.srcmc.rctmod.fabric.server.ModServer;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.gitlab.srcmc.rctmod.world.items.TrainerCard;
import com.gitlab.srcmc.rctmod.world.loot.conditions.DefeatCountCondition;
import com.gitlab.srcmc.rctmod.world.loot.conditions.LevelRangeCondition;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class ModFabric implements ModInitializer {
    public static final EntityType<TrainerMob> TRAINER = Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(ModCommon.MOD_ID, "trainer"), TrainerMob.getEntityType());
    public static final LootItemConditionType LEVEL_RANGE = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new ResourceLocation(ModCommon.MOD_ID, "level_range"), new LootItemConditionType(new LevelRangeCondition.Serializer()));
    public static final LootItemConditionType DEFEAT_COUNT = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new ResourceLocation(ModCommon.MOD_ID, "defeat_count"), new LootItemConditionType(new DefeatCountCondition.Serializer()));
    public static final Item TRAINER_CARD = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(ModCommon.MOD_ID, "trainer_card"), new TrainerCard());
    public static final CreativeModeTab CREATIVE_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(ModCommon.MOD_ID), FabricItemGroup.builder()
    	.icon(() -> new ItemStack(TRAINER_CARD))
    	.title(Component.translatable("itemGroup." + ModCommon.MOD_ID))
        .displayItems((context, entries) -> entries.accept(TRAINER_CARD))
    	.build());

    @Override
    public void onInitialize() {
        RCTMod.init(new PlayerController(), new LootConditions(), new Configs(new ClientConfig(), new ServerConfig(), null));
        registerEntityAttributes();
        registerCommands();
        registerAdvancementCriteria();
        registerCobblemonEvents();
        ModServer.init();
    }

    static void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(TRAINER, TrainerMob.createAttributes().build());
    }

    static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlayerCommands.register(dispatcher);
            TrainerCommands.register(dispatcher);
        });
    }

    static void registerAdvancementCriteria() {
        CriteriaTriggers.register(DefeatCountTrigger.get());
    }

    static void registerCobblemonEvents() {
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, CobblemonHandler::handleBattleVictory);
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe(Priority.HIGHEST, CobblemonHandler::handleExperienceGained);
    }
}
