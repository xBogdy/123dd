package com.gitlab.srcmc.rctmod.client.screens.widgets;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.ModClient;
import com.mojang.authlib.GameProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PlayerInfoWidget extends AbstractWidget {
    private static int X = 8;
    private static int Y = 8;
    private static int W = 224;
    private static int H = 128;

    private Font font;
    private TrainerMobData.Type trainerType;
    private boolean allTypes = true;

    public PlayerInfoWidget(Font font) {
        super(X, Y, W, H, Component.empty());
        this.font = font;
        this.active = false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        guiGraphics.blit(new ResourceLocation(ModCommon.MOD_ID, "textures/gui/trainer_card.png"), this.getX(), this.getY(), 0, 0, 224, 128);
        guiGraphics.drawString(this.font, Component.literal(localPlayer.getDisplayName().getString()).withStyle(ChatFormatting.ITALIC), this.getX() + 12, this.getY() + 12, 16);
        guiGraphics.drawString(this.font, Component.literal("Level Cap"), this.getX() + 12, this.getY() + 108, 16);
        guiGraphics.drawString(this.font, Component.literal(String.valueOf(playerState.getLevelCap())).withStyle(ChatFormatting.ITALIC), this.getX() + 72, this.getY() + 108, 16);
        guiGraphics.drawString(this.font, Component.literal("Total"), this.getX() + 100, this.getY() + 108, 16);
        guiGraphics.drawString(this.font, Component.literal(String.valueOf(String.format("%9d", this.getTotalDefeats()))).withStyle(ChatFormatting.ITALIC), this.getX() + 116, this.getY() + 108, 16);
        PlayerFaceRenderer.draw(guiGraphics, localPlayer.getSkinTextureLocation(), this.getX() + 8 + 4, this.getY() + 32, 72);
    }

    public void setTrainerType(TrainerMobData.Type trainerType) {
        this.trainerType = trainerType;
    }

    public void setAllTypes(boolean allTypes) {
        this.allTypes = allTypes;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    private long getTotalDefeats() {
        if(!this.allTypes) {
            return this.getTotalDefeats(this.trainerType);
        }

        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        return playerState.getTrainerDefeatCounts().values().stream().mapToLong(i -> i).reduce(0, Math::addExact);
    }

    private long getTotalDefeats(TrainerMobData.Type type) {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        return playerState.getTypeDefeatCount(type);
    }
}
