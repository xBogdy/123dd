package com.gitlab.srcmc.rctmod.client.screens;

import net.minecraft.client.gui.screens.Screen;

public enum ScreenType {
    TRAINER_CARD_SCREEN;

    private static final Screen[] screens = {
        new TrainerCardScreen()
    };

    public Screen getScreen() {
        return screens[this.ordinal()];
    }
}
