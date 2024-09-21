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

import java.util.function.Supplier;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

public class TargetArrowRenderer {
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
        0.0f, 0.0f, 0.0f, // bottom back right
        0.0f, 0.0f, 0.0f, // bottom back left
        0.0f, 0.0f, 0.0f, // bottom front left
        1.0f, 0.0f, 0.0f, // top
        0.0f, 0.0f, 0.0f, // bottom front right
        0.0f, 0.0f, 0.0f, // bottom back right
        0.0f, 0.0f, 0.0f  // bottom front left
    };

    private static final float PYRAMID_ALPHA = 0.5f;
    private static final Vector3f PYRAMID_UP = new Vector3f(0, 1, 0);
    private static final int TICKS_TO_ACTIVATE = 40;

    public static double TX = -0.15, TY = 0.25, TZ = 0.03;
    private static final float SX = 0.009375f, SY = 0.03f, SZ = 0.01875f;

    private Supplier<Item> itemSupplier;
    private Vector3f direction;
    private int sourceTicks, activationTicks;
    private boolean active;

    // public static double x = 0.4, y = -0.27, z = -0.66; // with RenderHand
    // public static double x = -0.15, y = 0.25, z = 0.03; // with ItemRenderer

    private static Supplier<TargetArrowRenderer> instanceSupplier = () -> {
        throw new IllegalStateException(TargetArrowRenderer.class.getName() + " not initialized");
    };

    public static void init(Supplier<Item> triggerItemSupplier) {
        var instance = new TargetArrowRenderer(triggerItemSupplier);
        instanceSupplier = () -> instance;
    }

    public static TargetArrowRenderer getInstance() {
        return instanceSupplier.get();
    }

    private TargetArrowRenderer(Supplier<Item> triggerItemSupplier) {
        this.itemSupplier = triggerItemSupplier;
    }

    public void tick() {
        this.updateTarget();

        if(this.active) {
            if(this.activationTicks < TICKS_TO_ACTIVATE) {
                this.activationTicks++;
            }
        } else {
            if(this.activationTicks > 0) {
                this.activationTicks--;
            }
        }
    }

    public void render(PoseStack poseStack, float partialTick) {
        if(this.activationTicks > 0) {
            var mc = Minecraft.getInstance();
            var cam = mc.gameRenderer.getMainCamera();

            if(cam.isInitialized()) {
                var t = Math.min(this.activationTicks + partialTick, TICKS_TO_ACTIVATE)/TICKS_TO_ACTIVATE;
                var rotation = new Quaternionf();
                this.direction.rotationTo(PYRAMID_UP, rotation);

                poseStack.pushPose();
                poseStack.translate(TX, TY, TZ);
                poseStack.mulPose(cam.rotation().difference(rotation).rotateLocalX((float)(2*cam.getXRot() * Math.PI / 180)));
                poseStack.mulPose(new Quaternionf().rotateY((float)Math.PI*(this.sourceTicks + partialTick)/TICKS_TO_ACTIVATE));
                poseStack.scale(SX * t, (float)(SY + Math.sin((this.sourceTicks + partialTick)/10)*0.01) * t, SZ * t);

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

    private void updateTarget() {
        var mc = Minecraft.getInstance();
        var player = mc.player;

        if(player != null)  {
            if(player.getMainHandItem().is(itemSupplier.get())) {
                this.setTarget(player, PlayerState.get(player).getTarget());
            } else {
                this.setTarget(player, null);
            }
        }
    }

    private void setTarget(Entity source, Entity target) {
        sourceTicks = source.tickCount;
        active = target != null;

        if(active) {
            direction = target.position().subtract(source.position()).toVector3f();
        }
    }
}
