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
package com.gitlab.srcmc.rctmod.client.screens;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.client.screens.widgets.PlayerInfoWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TrainerCardScreen extends Screen {
    private static final int CARD_W = 224;
    private static final int CARD_H = 128;

    private PlayerInfoWidget playerInfo;

    public TrainerCardScreen() {
        super(Component.literal("Trainer Card"));
        var mc = Minecraft.getInstance();
        this.font = mc.font;
    }

    @Override
    protected void init() {
        super.init();

        var window = Minecraft.getInstance().getWindow();
        this.width = window.getGuiScaledWidth();
        this.height = window.getGuiScaledHeight();
        
        var cfg = RCTMod.get().getClientConfig();
        var pad = cfg.trainerCardPadding();
        var card_x = pad + (int)((this.width - (CARD_W + 2*pad))*cfg.trainerCardAlignmentX());
        var card_y = pad + (int)((this.height - (CARD_H + 2*pad))*cfg.trainerCardAlignmentY());

        this.playerInfo = new PlayerInfoWidget(card_x, card_y, CARD_W, CARD_H, this.font);
        this.addRenderableOnly(this.playerInfo);
        for(var r : this.playerInfo.getRenderableOnlies()) this.addRenderableOnly(r);
        for(var w : this.playerInfo.getRenderableWidgets()) this.addRenderableWidget(w);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        var mc = Minecraft.getInstance();

        if(mc.options.keyInventory.matches(key, scancode)) {
            mc.setScreen(null);
            return true;
        }

        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if(i == 1) {
            var mc = Minecraft.getInstance();
            mc.setScreen(null);
            return true;
        }

        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.playerInfo.tick();
    }
}
