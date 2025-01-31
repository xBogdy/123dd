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
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.gitlab.srcmc.rctmod.world.items.TrainerCard;
import com.gitlab.srcmc.rctmod.world.loot.conditions.DefeatCountCondition;
import com.gitlab.srcmc.rctmod.world.loot.conditions.LevelRangeCondition;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Bus.MOD)
public class ModRegistries {
    public static class Sounds {
        public static final DeferredRegister<SoundEvent> REGISTRY;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModCommon.MOD_ID);
        }
    }

    public static class Items {
        public static final DeferredRegister<Item> REGISTRY;
        public static final RegistryObject<Item> TRAINER_CARD;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModCommon.MOD_ID);
            TRAINER_CARD = REGISTRY.register("trainer_card", TrainerCard::new);
        }
    }

    public static class Blocks {
        public static final DeferredRegister<Block> REGISTRY;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ModCommon.MOD_ID);
        }

        public static class Entities {
            public static final DeferredRegister<BlockEntityType<?>> REGISTRY;

            static {
                REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModCommon.MOD_ID);
            }
        }
    }

    public static class Entities {
        public static final DeferredRegister<EntityType<?>> REGISTRY;
        public static final RegistryObject<EntityType<TrainerMob>> TRAINER;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModCommon.MOD_ID);
            TRAINER = REGISTRY.register("trainer", TrainerMob::getEntityType);
        }
    }

    public static class LootItemConditions {
        public static final DeferredRegister<LootItemConditionType> REGISTRY;
        public static final RegistryObject<LootItemConditionType> LEVEL_RANGE;
        public static final RegistryObject<LootItemConditionType> DEFEAT_COUNT;

        static {
            REGISTRY = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ModCommon.MOD_ID);
            LEVEL_RANGE = REGISTRY.register("level_range", () -> new LootItemConditionType(new LevelRangeCondition.Serializer()));
            DEFEAT_COUNT = REGISTRY.register("defeat_count", () -> new LootItemConditionType(new DefeatCountCondition.Serializer()));
        }
    }

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent event) {
        registerBlockItems();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Sounds.REGISTRY.register(bus);
        Items.REGISTRY.register(bus);
        Blocks.REGISTRY.register(bus);
        Blocks.Entities.REGISTRY.register(bus);
        Entities.REGISTRY.register(bus);
        LootItemConditions.REGISTRY.register(bus);
    }

    private static ResourceKey<CreativeModeTab> modCreativeTab;
    
    @SubscribeEvent
    public static void onCreativeTabsRegister(RegisterEvent event) {
        if(!Items.REGISTRY.getEntries().isEmpty()) {
            modCreativeTab = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                new ResourceLocation(ModCommon.MOD_ID, "creative_tab"));

            event.register(Registries.CREATIVE_MODE_TAB, registerHelper ->
                registerHelper.register(modCreativeTab,
                    CreativeModeTab.builder().title(Component.translatable("itemGroup." + ModCommon.MOD_ID))
                        .icon(() -> Items.TRAINER_CARD.get().getDefaultInstance())
                        .displayItems((params, output) -> {
                            ModRegistries.Items.REGISTRY.getEntries()
                                .stream().filter(iro -> true) // all items by default
                                .forEach(iro -> output.accept(iro.get()));
                        }).build()));
        }
    }

    private static void registerBlockItems() {
        ModRegistries.Blocks.REGISTRY.getEntries()
            .stream().filter(bro -> true) // all blocks by default
            .forEach(bro -> Items.REGISTRY.register(
                bro.getId().getPath(),
                () -> new BlockItem(bro.get(), new Item.Properties())));
    }
}
