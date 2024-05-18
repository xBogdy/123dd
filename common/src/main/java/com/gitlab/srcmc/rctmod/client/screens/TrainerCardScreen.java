package com.gitlab.srcmc.rctmod.client.screens;

import com.gitlab.srcmc.rctmod.client.screens.widgets.PlayerInfoWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TrainerCardScreen extends Screen {
    private static final int SCREEN_X = 8;
    private static final int SCREEN_Y = 8;
    private static final int SCREEN_W = 224;
    private static final int SCREEN_H = 128;

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
        this.width = window.getWidth();
        this.height = window.getHeight();
        this.playerInfo = new PlayerInfoWidget(SCREEN_X, SCREEN_Y, SCREEN_W, SCREEN_H, this.font);
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
