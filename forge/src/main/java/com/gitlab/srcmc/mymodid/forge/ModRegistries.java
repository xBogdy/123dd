package com.gitlab.srcmc.mymodid.forge;

import com.gitlab.srcmc.mymodid.ModCommon;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

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

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModCommon.MOD_ID);
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

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModCommon.MOD_ID);
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
    }

    @SubscribeEvent
    public static void onCreativeTabsRegister(CreativeModeTabEvent.Register event) {
        if(!Items.REGISTRY.getEntries().isEmpty()) {
            event.registerCreativeModeTab(new ResourceLocation(ModCommon.MOD_ID), builder ->
                builder.title(Component.translatable("itemGroup." + ModCommon.MOD_ID))
                    .icon(() -> net.minecraft.world.item.Items.END_CRYSTAL.getDefaultInstance())
                    .displayItems((enabledFlags, populator, hasPermissions) -> {
                        ModRegistries.Items.REGISTRY.getEntries()
                            .stream().filter(iro -> true) // all items by default
                            .forEach(iro -> populator.accept(() -> iro.get()));
                    }));
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
