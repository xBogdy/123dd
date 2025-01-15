/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2025, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctmod.client.renderer;

import org.joml.Quaternionf;

import com.gitlab.srcmc.rctmod.world.blocks.entities.TrainerSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.world.item.ItemDisplayContext;

public class TrainerSpawnerBlockEntityRenderer implements BlockEntityRenderer<TrainerSpawnerBlockEntity> {
    public TrainerSpawnerBlockEntityRenderer(Context context) {
    }

    @Override
    public void render(TrainerSpawnerBlockEntity be, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        var item = be.getRenderItem();

        if(item != null) {
            var p = (be.getLevel().getGameTime() + f) * Math.PI / 60;
            poseStack.pushPose();
            poseStack.translate(0.5, 0.425 + Math.sin(p/2) * 0.05, 0.5);
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.mulPose(new Quaternionf().rotateLocalY((float)p));
            Minecraft.getInstance().getItemRenderer().renderStatic(item.getDefaultInstance(), ItemDisplayContext.GROUND, i, j, poseStack, multiBufferSource, be.getLevel(), 0);
            poseStack.popPose();
        }
    }
}
