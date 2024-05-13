package com.gitlab.srcmc.rctmod.client.screens.widgets;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.ModClient;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
        int w = 128;

        guiGraphics.blit(new ResourceLocation(ModCommon.MOD_ID, "textures/item/trainer_card"), x, y, 0, 0, 234, 128);

        guiGraphics.drawString(font, Component.literal("Player: " + localPlayer.getUUID()), x, y, h);
        y += h;

        guiGraphics.drawString(font, Component.literal("Level Cap: "), x, y, h);
        x += w;

        guiGraphics.drawString(font, Component.literal(String.valueOf(playerState.getLevelCap())), x, y, h);
        x = 8;
        y += h;

        guiGraphics.drawString(font, Component.literal("Trainer Defeats: "), x, y, h);
        x += w;

        guiGraphics.drawString(font, Component.literal(String.valueOf(playerState.getTypeDefeatCounts().values().stream().reduce(0, (a, b) -> a + b))), x, y, h);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
