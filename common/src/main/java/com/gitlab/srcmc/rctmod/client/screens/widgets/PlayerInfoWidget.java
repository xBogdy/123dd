package com.gitlab.srcmc.rctmod.client.screens.widgets;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.ModClient;
import net.minecraft.ChatFormatting;
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

    private static final Component LEVEL_CAP_LABEL = Component.literal("Level Cap");
    private static final Component TOTAL_DEFEATS_LABEL = Component.literal("Total");

    private Font font;
    private TrainerMobData.Type trainerType;
    private boolean allTypes = true;
    private Component displayName = Component.literal("Trainer").withStyle(ChatFormatting.OBFUSCATED);
    private Component levelCap = Component.literal("100").withStyle(ChatFormatting.OBFUSCATED);
    private Component totalDefeats = Component.literal("100").withStyle(ChatFormatting.OBFUSCATED);
    private ResourceLocation skinLocation = new ResourceLocation("textures/entity/player/wide/steve.png");

    public PlayerInfoWidget(Font font) {
        super(X, Y, W, H, Component.empty());
        this.font = font;
        this.active = false;
    }

    public void tick() {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        this.displayName = Component.literal(localPlayer.getDisplayName().getString()).withStyle(ChatFormatting.ITALIC);
        this.levelCap = Component.literal(String.valueOf(playerState.getLevelCap()));
        this.totalDefeats = Component.literal(String.valueOf(String.format("%9d", this.getTotalDefeats())));
        this.skinLocation = localPlayer.getSkinTextureLocation();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blit(new ResourceLocation(ModCommon.MOD_ID, "textures/gui/trainer_card.png"), this.getX(), this.getY(), 0, 0, 224, 128);
        guiGraphics.drawString(this.font, this.displayName, this.getX() + 12, this.getY() + 12, 16);
        guiGraphics.drawString(this.font, LEVEL_CAP_LABEL, this.getX() + 12, this.getY() + 108, 16);
        guiGraphics.drawString(this.font, this.levelCap, this.getX() + 72, this.getY() + 108, 16);
        guiGraphics.drawString(this.font, TOTAL_DEFEATS_LABEL, this.getX() + 100, this.getY() + 108, 16);
        guiGraphics.drawString(this.font, this.totalDefeats, this.getX() + 116, this.getY() + 108, 16);
        PlayerFaceRenderer.draw(guiGraphics, this.skinLocation, this.getX() + 8 + 4, this.getY() + 32, 72);
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
        return playerState.getTypeDefeatCounts().values().stream().mapToLong(i -> i).reduce(0, Math::addExact);
    }

    private long getTotalDefeats(TrainerMobData.Type type) {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        return playerState.getTypeDefeatCount(type);
    }
}
