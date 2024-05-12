package com.gitlab.srcmc.rctmod.client.screens.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class TrainerListWidget extends AbstractWidget {
    private Font font;

    public TrainerListWidget(Font font, int x, int y, int w, int h, Component component) {
        super(x, y, w, h, component);
        this.font = font;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        var trainers = new String[] {
            "Trainer 1",
            "Trainer 2",
            "Trainer 3"
        };

        int x = 0;
        int y = 0;

        for(var trainer : trainers) {
            guiGraphics.drawString(font, Component.literal(trainer), x, y, 16);
            y += 16;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
