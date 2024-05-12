package com.gitlab.srcmc.rctmod.client.screens;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.client.screens.widgets.PlayerInfoWidget;
import com.gitlab.srcmc.rctmod.client.screens.widgets.TrainerListWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TrainerCardScreen extends Screen {
    private EditBox editBox;
    private PlayerInfoWidget playerInfo;

    public TrainerCardScreen() {
        super(Component.literal("Trainer Card"));
    }

    @Override
    protected void init() {
        super.init();

        // Add widgets and precomputed values
        var window = Minecraft.getInstance().getWindow();
        ModCommon.LOG.info("WINDOW: " + String.format("x: %d, y: %d, w: %d, h: %d", window.getX(), window.getY(), window.getWidth(), window.getHeight()));
        this.width = window.getWidth();
        this.height = window.getHeight();

        // this.editBox = new EditBox(this.font, this.width/2 - this.width/4, this.height/2, this.width/2, 16, title);
        // this.addRenderableWidget(editBox);
        // this.addRenderableWidget(new TrainerListWidget(this.font, 8, 8, this.width - 8, this.height - 8, Component.literal("Trainer List")));

        this.playerInfo = new PlayerInfoWidget(this.font, 8, 8, this.width - 8, this.height - 8);
        this.addRenderableWidget(playerInfo);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        Minecraft.getInstance().setScreen(null);
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        // this.editBox.tick();
    }
}
