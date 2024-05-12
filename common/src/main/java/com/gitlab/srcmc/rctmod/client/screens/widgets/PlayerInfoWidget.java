package com.gitlab.srcmc.rctmod.client.screens.widgets;

import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.ModClient;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class PlayerInfoWidget extends AbstractWidget {
    private Font font;

    public PlayerInfoWidget(Font font, int x, int y, int w, int h) {
        super(x, y, w, h, Component.literal("Player Info"));
        this.font = font;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        var localPlayer = ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);

        int x = 8;
        int y = 8;
        int h = 16;

        var str = new StringBuilder();
        str.append(String.format("Player UUID    : %s\n", localPlayer.getUUID()));
        str.append(String.format("Level Cap      : %d\n", playerState.getLevelCap()));
        str.append(String.format("Trainer Defeats: %d\n", playerState.getTypeDefeatCounts().values().stream().reduce(0, (a, b) -> a + b)));
        guiGraphics.drawString(font, Component.literal(str.toString()), x, y, h);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
