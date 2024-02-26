package com.gitlab.srcmc.mymodid.forge;

import com.gitlab.srcmc.mymodid.ModCommon;

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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

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
            REGISTRY.register("rxample_item", () -> new Item(new Item.Properties()));
        }
    }

    public static class Blocks {
        public static final DeferredRegister<Block> REGISTRY;

        static {
            REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, ModCommon.MOD_ID);
            REGISTRY.register("example_block", () -> new Block(BlockBehaviour.Properties.of()));
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
                        .icon(() -> net.minecraft.world.item.Items.END_CRYSTAL.getDefaultInstance())
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
