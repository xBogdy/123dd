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

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;

public class CustomStringWidget extends StringWidget {
    private float scale = 1f, autoScale = 1f;
    private float halign, valign = 0.5f;
    private boolean clip, fit;

    public CustomStringWidget(Component component, Font font) {
        super(component, font);
    }

    public CustomStringWidget(int i, int j, Component component, Font font) {
        super(i, j, component, font);
    }

    public CustomStringWidget(int i, int j, int k, int l, Component component, Font font) {
        super(i, j, k, l, component, font);
    }

    @Override
    public void setMessage(Component component) {
        var w = this.getWidth();
        var h = this.getHeight();
        var cw = this.getFont().width(component);
        var ch = this.getFont().lineHeight;
        this.autoScale = Math.min(cw > w ? w/(float)cw : 1f, ch > h ? h/(float)ch : 1f);
        super.setMessage(component);
    }

    public boolean getClip() {
        return this.clip;
    }

    public boolean getFit() {
        return this.fit;
    }

    public float getScale() {
        return this.scale;
    }

    public float getHAlign() {
        return this.halign;
    }

    public float setVAlign() {
        return this.valign;
    }

    public void setClip(boolean clip) {
        this.clip = clip;
    }

    public void setFit(boolean fit) {
        this.fit = fit;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setHAlign(float halign) {
        this.halign = halign;
    }

    public void setVAlign(float valign) {
        this.valign = valign;
    }

    public CustomStringWidget clipped(boolean clip) {
        this.setClip(clip);
        return this;
    }

    public CustomStringWidget fitting(boolean fit) {
        this.setFit(fit);
        return this;
    }

    public CustomStringWidget scaled(float scale) {
        this.setScale(scale);
        return this;
    }

    public CustomStringWidget alignCenter() {
        this.halign = 0.5f;
        this.valign = 0.5f;
        return this;
    }

    public CustomStringWidget alignLeft() {
        this.halign = 0f;
        return this;
    }

    public CustomStringWidget alignRight() {
        this.halign = 1f;
        return this;
    }

    public CustomStringWidget alignTop() {
        this.valign = 0f;
        return this;
    }

    public CustomStringWidget alignBottom() {
        this.valign = 1f;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        var clip = this.clip;
        var scale = this.fit ? this.scale*this.autoScale : this.scale;
        var x = this.getX();
        var y = this.getY();
        var w = this.getWidth();
        var h = this.getHeight();

        if(clip) {
            guiGraphics.enableScissor(x, y, x + w, y + h);
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + (w - this.getFont().width(this.getMessage())*scale)*halign, y + (h - this.getFont().lineHeight*scale)*valign, 0);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.pose().translate(-x, -y, 0);
        guiGraphics.drawString(this.getFont(), this.getMessage(), x, y, this.getColor());
        guiGraphics.pose().popPose();

        if(clip) {
            guiGraphics.disableScissor();
        }
    }
}
