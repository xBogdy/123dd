package com.gitlab.srcmc.rctmod.client.renderer;

import com.gitlab.srcmc.rctmod.ModCommon;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.WanderingTrader;

public class TrainerAssociationRenderer extends WanderingTraderRenderer {
    public static final ResourceLocation TEXUTURE = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/entity/trainer_association.png");

    public TrainerAssociationRenderer(Context context) {
        super(context);
    }
    
    public ResourceLocation getTextureLocation(WanderingTrader wanderingTrader) {
        return TEXUTURE;
    }    
}
