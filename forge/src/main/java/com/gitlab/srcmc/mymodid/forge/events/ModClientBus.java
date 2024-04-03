package com.gitlab.srcmc.mymodid.forge.events;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.client.TrainerRenderer;
import com.gitlab.srcmc.mymodid.forge.ModRegistries;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModCommon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientBus {
    @SubscribeEvent
    static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModRegistries.Entities.TRAINER.get(), TrainerRenderer::new);
    }
}
