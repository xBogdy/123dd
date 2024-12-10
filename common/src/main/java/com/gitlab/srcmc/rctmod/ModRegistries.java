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
package com.gitlab.srcmc.rctmod;

import com.gitlab.srcmc.rctmod.advancements.criteria.DefeatCountTrigger;
import com.gitlab.srcmc.rctmod.world.blocks.TrainerSpawnerBlock;
import com.gitlab.srcmc.rctmod.world.blocks.entities.TrainerSpawnerBlockEntity;
import com.gitlab.srcmc.rctmod.world.entities.TrainerMob;
import com.gitlab.srcmc.rctmod.world.items.TrainerCard;
import com.gitlab.srcmc.rctmod.world.loot.conditions.DefeatCountCondition;
import com.gitlab.srcmc.rctmod.world.loot.conditions.LevelRangeCondition;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class ModRegistries {
    public class Entities {
        public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.ENTITY_TYPE);
        public static final RegistrySupplier<EntityType<TrainerMob>> TRAINER;

        static {
            TRAINER = REGISTRY.register(location("trainer"), TrainerMob::getEntityType);
        }
    }

    public class Items {
        public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.ITEM);
        public static final RegistrySupplier<TrainerCard> TRAINER_CARD;
        public static final RegistrySupplier<BlockItem> TRAINER_SPAWNER;

        static {
            TRAINER_CARD = REGISTRY.register(location("trainer_card"), TrainerCard::new);
            TRAINER_SPAWNER = REGISTRY.register(location("trainer_spawner"), () -> new BlockItem(Blocks.TRAINER_SPAWNER.get(), new Item.Properties()));
        }
    }

    public class Blocks {
        public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.BLOCK);
        public static final RegistrySupplier<TrainerSpawnerBlock> TRAINER_SPAWNER;

        static {
            TRAINER_SPAWNER = REGISTRY.register(location("trainer_spawner"), TrainerSpawnerBlock::new);
        }
    }

    public class BlockEntityTypes {
        public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
        public static final RegistrySupplier<BlockEntityType<TrainerSpawnerBlockEntity>> TRAINER_SPAWNER;

        static {
            TRAINER_SPAWNER = REGISTRY.register(location("trainer_spawner"), () -> BlockEntityType.Builder.of(TrainerSpawnerBlockEntity::new, Blocks.TRAINER_SPAWNER.get()).build(null));
        }
    }
    
    public class LootItemConditions {
        public static final DeferredRegister<LootItemConditionType> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.LOOT_CONDITION_TYPE);
        public static final RegistrySupplier<LootItemConditionType> LEVEL_RANGE;
        public static final RegistrySupplier<LootItemConditionType> DEFEAT_COUNT;

        static {
            LEVEL_RANGE = REGISTRY.register(location("level_range"), () -> new LootItemConditionType(LevelRangeCondition.CODEC));
            DEFEAT_COUNT = REGISTRY.register(location("defeat_count"), () -> new LootItemConditionType(DefeatCountCondition.CODEC));
        }
    }

    public class CriteriaTriggers {
        public static final DeferredRegister<CriterionTrigger<?>> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.TRIGGER_TYPE);
        public static final RegistrySupplier<DefeatCountTrigger> DEFEAT_COUNT;

        static {
            DEFEAT_COUNT = REGISTRY.register(location("defeat_count"), DefeatCountTrigger::new);
        }
    }

    public class CreativeTabs {
        public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.CREATIVE_MODE_TAB);
        public static final RegistrySupplier<CreativeModeTab> CREATIVE_TAB;

        static {
            CREATIVE_TAB = REGISTRY.register(location("creative_tab"), () -> CreativeTabRegistry.create(builder -> builder
                .icon(() -> Items.TRAINER_CARD.get().getDefaultInstance())
                .title(Component.translatable("itemGroup." + ModCommon.MOD_ID))
                .displayItems((context, entries) ->{
                    entries.accept(Items.TRAINER_CARD.get().getDefaultInstance());
                    entries.accept(Items.TRAINER_SPAWNER.get().getDefaultInstance());
                }).build()));
        }
    }

    public static void init() {
        if(!ModRegistries.initialized) {
            Entities.REGISTRY.register();
            Blocks.REGISTRY.register();
            Items.REGISTRY.register();
            BlockEntityTypes.REGISTRY.register();
            LootItemConditions.REGISTRY.register();
            CriteriaTriggers.REGISTRY.register();
            CreativeTabs.REGISTRY.register();
            EntityAttributeRegistry.register(Entities.TRAINER, TrainerMob::createAttributes);
            ModRegistries.initialized = true;
        }
    }

    private static ResourceLocation location(String key) {
        return ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, key);
    }

    private static boolean initialized;
    private ModRegistries() {}
}
