package com.gitlab.srcmc.rctmod.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gitlab.srcmc.rctmod.client.renderer.TargetArrowRenderer;
import com.gitlab.srcmc.rctmod.world.items.TrainerCard;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "render", at = @At("RETURN"), remap = true)
    public void onRender(ItemStack itemStack, ItemDisplayContext displayContext, boolean rotationReversed, PoseStack poseStack, MultiBufferSource bufferSource, int i1, int i2, BakedModel bakedModel, CallbackInfo ci) {
        if(displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && itemStack.getItem() instanceof TrainerCard) {
            TargetArrowRenderer.getInstance().render(poseStack, 0);
        }
    }
}
