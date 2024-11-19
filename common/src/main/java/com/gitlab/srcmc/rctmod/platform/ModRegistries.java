package com.gitlab.srcmc.rctmod.platform;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.advancements.criteria.DefeatCountTrigger;
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
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class ModRegistries {
    public class Entities {
        public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.ENTITY_TYPE);
        public static final RegistrySupplier<EntityType<TrainerMob>> TRAINER;

        static {
            TRAINER = REGISTRY.register(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "trainer"), TrainerMob::getEntityType);
        }
    }

    public class Items {
        public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.ITEM);
        public static final RegistrySupplier<Item> TRAINER_CARD;

        static {
            TRAINER_CARD = REGISTRY.register(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "trainer_card"), TrainerCard::new);
        }
    }
    
    public class LootItemConditions {
        public static final DeferredRegister<LootItemConditionType> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.LOOT_CONDITION_TYPE);
        public static final RegistrySupplier<LootItemConditionType> LEVEL_RANGE;
        public static final RegistrySupplier<LootItemConditionType> DEFEAT_COUNT;

        static {
            LEVEL_RANGE = REGISTRY.register(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "level_range"), () -> new LootItemConditionType(LevelRangeCondition.CODEC));
            DEFEAT_COUNT = REGISTRY.register(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "defeat_count"), () -> new LootItemConditionType(DefeatCountCondition.CODEC));
        }
    }

    public class CriteriaTriggers {
        public static final DeferredRegister<CriterionTrigger<?>> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.TRIGGER_TYPE);
        public static final RegistrySupplier<DefeatCountTrigger> DEFEAT_COUNT;

        static {
            DEFEAT_COUNT = REGISTRY.register(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "defeat_count"), DefeatCountTrigger::new);
        }
    }

    public class CreativeTabs {
        public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(ModCommon.MOD_ID, Registries.CREATIVE_MODE_TAB);
        public static final RegistrySupplier<CreativeModeTab> CREATIVE_TAB;

        static {
            CREATIVE_TAB = REGISTRY.register(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "creative_tab"), () -> CreativeTabRegistry.create(builder -> builder
                .icon(() -> Items.TRAINER_CARD.get().getDefaultInstance())
                .title(Component.translatable("itemGroup." + ModCommon.MOD_ID))
                .displayItems((context, entries) -> entries.accept(Items.TRAINER_CARD.get().getDefaultInstance()))
                .build()));
        }
    }

    public static void init() {
        ModCommon.LOG.info("INITIALIZING REGISTRIES: " + !ModRegistries.initialized);

        if(!ModRegistries.initialized) {
            Entities.REGISTRY.register();
            Items.REGISTRY.register();
            LootItemConditions.REGISTRY.register();
            CriteriaTriggers.REGISTRY.register();
            CreativeTabs.REGISTRY.register();
            EntityAttributeRegistry.register(Entities.TRAINER, TrainerMob::createAttributes);
            ModRegistries.initialized = true;
        }
    }

    private static boolean initialized;
    private ModRegistries() {}
}
