package com.gitlab.srcmc.mymodid.client;

import com.gitlab.srcmc.mymodid.ModCommon;
import com.gitlab.srcmc.mymodid.world.entities.TrainerMob;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TrainerRenderer extends HumanoidMobRenderer<TrainerMob, HumanoidModel<TrainerMob>> {
    public TrainerRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new HumanoidModel<>(pContext.bakeLayer(ModelLayers.PLAYER)), 1f);
    }

    @Override
    public ResourceLocation getTextureLocation(TrainerMob mob) {
        return ModCommon.TRAINER_MANAGER.getData(mob).getTextureResource();
    }

    @Override
    public void render(TrainerMob pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }
}

// public class TrainerRenderer extends MobRenderer<Trainer, TrainerModel<Trainer>> {
//     public TrainerRenderer(EntityRendererProvider.Context pContext) {
//         super(pContext, new TrainerModel<>(pContext.bakeLayer(ModelLayers.TRAINER_LAYER)), 1f);
//     }

//     @Override
//     public ResourceLocation getTextureLocation(Trainer Trainer) {
//         return new ResourceLocation(ModCommon.MOD_ID, "textures/entity/trainer.png");
//     }

//     @Override
//     public void render(Trainer pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
//         super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
//     }
// }
