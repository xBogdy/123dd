/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
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
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;

public final class TargetArrowRenderer {
    private static final float PYRAMID_STRIP[] = {
         0,  1,  0, // top
         1, -1,  1, // bottom back right
        -1, -1,  1, // bottom back left
        -1, -1, -1, // bottom front left
         0,  1,  0, // top
         1, -1, -1, // bottom front right
         1, -1,  1, // bottom back right
        -1, -1, -1  // bottom front left
    };

    private static final float PYRAMID_COLORS[] = {
        1.0f, 0.0f, 0.0f, // top
        0.0f, 1.0f, 0.0f, // bottom back right
        0.0f, 1.0f, 0.0f, // bottom back left
        0.0f, 0.0f, 1.0f, // bottom front left
        1.0f, 0.0f, 0.0f, // top
        0.0f, 0.0f, 1.0f, // bottom front right
        0.0f, 1.0f, 0.0f, // bottom back right
        0.0f, 0.0f, 1.0f  // bottom front left
    };

    private static final float PYRAMID_ALPHA = 0.5f;
    private static final Vector3f PYRAMID_UP = new Vector3f(0, 1, 0);
    private static final int TICKS_TO_ACTIVATE = 40;
    private static Vector3f direction;
    private static int sourceTicks, activationTicks;
    private static boolean active;

    public static void setTarget(Entity source, Entity target) {
        sourceTicks = source.tickCount;
        active = target != null;

        if(active) {
            direction = target.position().subtract(source.position()).toVector3f();
        }
    }

    public static void tick() {
        if(active) {
            if(activationTicks < TICKS_TO_ACTIVATE) {
                activationTicks++;
            }
        } else {
            if(activationTicks > 0) {
                activationTicks--;
            }
        }
    }

    public static void render(PoseStack poseStack, float partialTick) {
        if(activationTicks > 0) {
            var mc = Minecraft.getInstance();
            var cam = mc.gameRenderer.getMainCamera();

            if(cam.isInitialized()) {
                var t = Math.min(activationTicks + partialTick, TICKS_TO_ACTIVATE)/TICKS_TO_ACTIVATE;
                var rotation = new Quaternionf();
                direction.rotationTo(PYRAMID_UP, rotation);

                poseStack.pushPose();
                poseStack.translate(1, -0.25, -1);
                poseStack.mulPose(cam.rotation().difference(rotation).rotateLocalX((float)(2*cam.getXRot() * Math.PI / 180)));
                poseStack.mulPose(new Quaternionf().rotateY((float)Math.PI*(sourceTicks + partialTick)/40));
                poseStack.scale(0.05f * t, 0.1f * t, 0.05f * t);

                var m = poseStack.last().pose();
                var tess = Tesselator.getInstance();
                var buffer = tess.getBuilder();

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

                for(int i = 0; i < PYRAMID_STRIP.length; i += 3) {
                    buffer.vertex(m, PYRAMID_STRIP[i], PYRAMID_STRIP[i + 1], PYRAMID_STRIP[i + 2])
                        .color(PYRAMID_COLORS[i], PYRAMID_COLORS[i + 1], PYRAMID_COLORS[i + 2], PYRAMID_ALPHA * t)
                        .endVertex();
                }

                poseStack.popPose();
                tess.end();
            }
        }
    }

    private TargetArrowRenderer() {}
}
