package com.gitlab.srcmc.mymodid.forge.events;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.forge.ModRegistries;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBus {
    @SubscribeEvent
    static void onCommonSetip(FMLCommonSetupEvent event) {
        ModCommon.TRAINER_MANAGER.load();
    }

    @SubscribeEvent
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(ModRegistries.Entities.TRAINER.get(), TrainerMob.createAttributes().build());
    }
}
