/*
 * This file is part of Example Mod.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * Example Mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Example Mod is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Example Mod. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.my_mod_id.forge;

import com.gitlab.srcmc.my_mod_id.ModCommon;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRegistries {
    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Sounds.REGISTRY.register(bus);
        Items.REGISTRY.register(bus);
        Blocks.REGISTRY.register(bus);
        BlockEntities.REGISTRY.register(bus);
    }

    public static class Sounds {
        public static final DeferredRegister<SoundEvent> REGISTRY;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModCommon.MOD_ID);
        }
    }

    public static class Items {
        public static final DeferredRegister<Item> REGISTRY;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModCommon.MOD_ID);
        }
    }

    public static class Blocks {
        public static final DeferredRegister<Block> REGISTRY;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ModCommon.MOD_ID);
        }
    }

    public static class BlockEntities {
        public static final DeferredRegister<BlockEntityType<?>> REGISTRY;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ModCommon.MOD_ID);
        }
    }
}
