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
package com.gitlab.srcmc.rctmod.client.screens.widgets;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public abstract class TrainerDataWidget extends AbstractScrollWidget implements IPaginated {
    public static final float INNER_SCALE = 0.65f;
    protected Font font;

    public TrainerDataWidget(int x, int y, int w, int h, Font font) {
        super(x, y, w, h, Component.empty());
        this.font = font;
    }

    @Override
    protected int getInnerHeight() {
        return this.getHeight() - this.totalInnerPadding();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int x, int y, float f) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) (this.getX() + this.innerPadding()), (float) (this.getY() + this.innerPadding()), 0.0F);
        guiGraphics.pose().scale(INNER_SCALE, INNER_SCALE, 1f);
        this.renderPage(guiGraphics, x, y, f);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        if(this.visible) {
            if(this.scrollbarVisible()) {
                super.renderBackground(guiGraphics);
            } else {
                this.renderBorder(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
                this.renderFullScrollBar(guiGraphics);
            }
        }
    }

    private int getContentHeight() {
        return this.getInnerHeight() + 4;
    }

    private int getScrollBarHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
    }

    private void renderFullScrollBar(GuiGraphics guiGraphics) {
        int i = this.getScrollBarHeight();
        int j = this.getX() + this.width;
        int k = this.getX() + this.width + 8;
        int l = this.getY() + this.height - i;
        int m = l + i;
        guiGraphics.fill(j, l, k, m, -8355712);
        guiGraphics.fill(j, l, k - 1, m - 1, -4144960);
    }

    @Override
    protected double scrollRate() {
        return 9.0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput arg0) {
        // TODO
    }

    protected double localX(double x) {
        return (x - this.getX()) / INNER_SCALE - this.innerPadding();
    }

    protected double localY(double y) {
        return (y - this.getY() + this.scrollAmount()) / INNER_SCALE - this.innerPadding();
    }

    protected static<T> MutableComponent toComponent(T value) {
        return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GREEN);
    }

    protected abstract void renderPage(GuiGraphics guiGraphics, int x, int y, float f);

    public class HoverElement<T extends Renderable & GuiEventListener> implements Renderable {
        public final T element;
        private final Consumer<T> onHoverStart;
        private final Consumer<T> onHoverEnd;

        public HoverElement(T element, Consumer<T> onHoverStart, Consumer<T> onHoverEnd) {
            this.element = element;
            this.onHoverStart = onHoverStart;
            this.onHoverEnd = onHoverEnd;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y, float f) {
            if(this.element.isMouseOver(localX(x), localY(y))) {
                this.onHoverStart.accept(this.element);
            } else {
                this.onHoverEnd.accept(this.element);
            }

            this.element.render(guiGraphics, x, y, f);
        }
    }
}
