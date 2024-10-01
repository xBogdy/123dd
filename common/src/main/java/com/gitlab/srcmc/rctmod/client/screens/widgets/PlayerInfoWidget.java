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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.client.ModClient;
import com.gitlab.srcmc.rctmod.client.screens.widgets.text.AutoScaledStringWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PlayerInfoWidget extends AbstractWidget {
    public enum Display { TRAINER_LIST, TRAINER_INFO }

    private static final ResourceLocation TRAINER_CARD_IMAGE_LOCATION = new ResourceLocation(ModCommon.MOD_ID, "textures/gui/trainer_card.png");
    private static final int TRAINER_CARD_IMAGE_X = 0;
    private static final int TRAINER_CARD_IMAGE_Y = 0;
    private static final int TRAINER_CARD_IMAGE_W = 224;
    private static final int TRAINER_CARD_IMAGE_H = 128;

    private static final int SKIN_X = 13;
    private static final int SKIN_Y = 32;
    private static final int SKIN_SIZE = 72;

    private static final int DISPLAY_NAME_X = 8 + 8;
    private static final int DISPLAY_NAME_Y = 8;
    private static final int DISPLAY_NAME_W = 82 - 16;
    private static final int DISPLAY_NAME_H = 16;

    private static final int LEVEL_CAP_X = 8;
    private static final int LEVEL_CAP_Y = 104;
    private static final int LEVEL_CAP_W = 82;
    private static final int LEVEL_CAP_H = 16;
    private static final int LEVEL_CAP_PADDING = 4;

    private static final int TOTAL_DEFEATS_X = 96;
    private static final int TOTAL_DEFEATS_Y = 104;
    private static final int TOTAL_DEFEATS_W = 88;
    private static final int TOTAL_DEFEATS_H = 16;
    private static final int TOTAL_DEFEATS_PADDING = 4;

    private static final int TRAINER_LIST_X = 96;
    private static final int TRAINER_LIST_Y = 32;
    private static final int TRAINER_LIST_W = 112;
    private static final int TRAINER_LIST_H = 72;

    private static final int TYPE_BUTTON_X = 96;
    private static final int TYPE_BUTTON_Y = 8;
    private static final int TYPE_BUTTON_W = 100;
    private static final int TYPE_BUTTON_H = 16;

    private static final int CHECKBOX_X = 196;
    private static final int CHECKBOX_Y = 8;
    private static final int CHECKBOX_W = 20; // min
    private static final int CHECKBOX_H = 16;

    private static final int NEXT_PAGE_BUTTON_X = 200;
    private static final int NEXT_PAGE_BUTTON_Y = 104;
    private static final int NEXT_PAGE_BUTTON_SIZE = 16;

    private static final String ALL_TRAINER_TYPES_STR = "ALL";

    private final StringWidget displayName;
    private final StringWidget levelCapLabel;
    private final StringWidget levelCapValue;
    private final StringWidget totalDefeatsLabel;
    private final StringWidget totalDefeatsValue;

    private final TrainerListWidget trainerList;
    private final TrainerInfoWidget trainerInfo;
    private final CycleButton<String> trainerTypeButton;
    private final Button nextPageButton;
    private final Button prevPageButton;
    private final Checkbox showUndefeated;

    private final AbstractWidget[] renderableWidgets;
    private final AbstractWidget[] renderableOnlies;

    private Boolean trainerListShowUndefeated;
    private Component trainerListType;

    private ResourceLocation skinLocation;
    private Font font;

    public PlayerInfoWidget(int x, int y, int w, int h, Font font) {
        super(x, y, w, h, Component.empty());
        this.active = false;
        this.font = font;

        this.displayName = new AutoScaledStringWidget(x + DISPLAY_NAME_X, y + DISPLAY_NAME_Y, DISPLAY_NAME_W, DISPLAY_NAME_H, Component.empty(), this.font).alignCenter().fitting(true);
        this.levelCapLabel = new StringWidget(x + LEVEL_CAP_X + LEVEL_CAP_PADDING, y + LEVEL_CAP_Y + LEVEL_CAP_H/8, LEVEL_CAP_W, LEVEL_CAP_H, Component.literal("Level Cap").withStyle(ChatFormatting.WHITE), this.font).alignLeft();
        this.levelCapValue = new StringWidget(x + LEVEL_CAP_X, y + LEVEL_CAP_Y + LEVEL_CAP_H/8, LEVEL_CAP_W - LEVEL_CAP_PADDING, LEVEL_CAP_H, Component.empty(), this.font).alignRight();
        this.totalDefeatsLabel = new StringWidget(x + TOTAL_DEFEATS_X + TOTAL_DEFEATS_PADDING, y + TOTAL_DEFEATS_Y + TOTAL_DEFEATS_H/8, TOTAL_DEFEATS_W, TOTAL_DEFEATS_H, Component.literal("Total").withStyle(ChatFormatting.WHITE), this.font).alignLeft();
        this.totalDefeatsValue = new StringWidget(x + TOTAL_DEFEATS_X, y + TOTAL_DEFEATS_Y + TOTAL_DEFEATS_H/8, TOTAL_DEFEATS_W - TOTAL_DEFEATS_PADDING, TOTAL_DEFEATS_H, Component.empty(), this.font).alignRight();
        this.trainerList = new TrainerListWidget(x + TRAINER_LIST_X, y + TRAINER_LIST_Y, TRAINER_LIST_W, TRAINER_LIST_H, font, sortedTrainerIds());
        this.trainerInfo = new TrainerInfoWidget(x + TRAINER_LIST_X, y + TRAINER_LIST_Y, TRAINER_LIST_W, TRAINER_LIST_H, font);

        var types = new ArrayList<String>();
        types.add(ALL_TRAINER_TYPES_STR);
        types.addAll(Stream.of(TrainerMobData.Type.values()).map(type -> type.name()).toList());

        this.showUndefeated = new Checkbox(x + CHECKBOX_X, y + CHECKBOX_Y, CHECKBOX_W, CHECKBOX_H, Component.empty(), true);
        this.trainerTypeButton = CycleButton.<String>builder(t -> Component.literal(t))
            .withValues(types).withInitialValue(ALL_TRAINER_TYPES_STR)
            .create(x + TYPE_BUTTON_X, y + TYPE_BUTTON_Y, TYPE_BUTTON_W, TYPE_BUTTON_H, Component.empty());
        
        this.nextPageButton = Button
            .builder(Component.literal(">"), this::onNextPage)
            .pos(x + NEXT_PAGE_BUTTON_X, y + NEXT_PAGE_BUTTON_Y)
            .size(NEXT_PAGE_BUTTON_SIZE, NEXT_PAGE_BUTTON_SIZE).build();

        this.prevPageButton = Button
            .builder(Component.literal("<"), this::onPrevPage)
            .pos(x + NEXT_PAGE_BUTTON_X - NEXT_PAGE_BUTTON_SIZE, y + NEXT_PAGE_BUTTON_Y)
            .size(NEXT_PAGE_BUTTON_SIZE, NEXT_PAGE_BUTTON_SIZE).build();

        this.renderableWidgets = new AbstractWidget[] {
            this.trainerList,
            this.trainerInfo,
            this.trainerTypeButton,
            this.showUndefeated,
            this.prevPageButton,
            this.nextPageButton
        };

        this.renderableOnlies = new AbstractWidget[] {
            this.displayName,
            this.levelCapLabel,
            this.levelCapValue,
            this.totalDefeatsLabel,
            this.totalDefeatsValue,
        };

        this.trainerList.setOnTrainerClicked((trainerNr, trainerId, entryState) -> {
            this.trainerInfo.initTrainerInfo(trainerNr, trainerId, entryState);
            this.trainerInfo.setPage(0);
            this.setDisplay(Display.TRAINER_INFO);
        });

        this.trainerInfo.setOnBackClicked(i -> this.setDisplay(Display.TRAINER_LIST));
        this.setDisplay(Display.TRAINER_LIST);
    }

    public AbstractWidget[] getRenderableWidgets() {
        return this.renderableWidgets;
    }

    public AbstractWidget[] getRenderableOnlies() {
        return this.renderableOnlies;
    }

    public void setDisplay(Display display) {
        switch (display) {
            case TRAINER_LIST:
                this.trainerInfo.visible = this.trainerInfo.active = false;
                this.trainerList.visible = this.trainerList.active = true;
                this.showUndefeated.active = true;
                this.trainerTypeButton.active = true;

                if(this.trainerListShowUndefeated != null) {
                    if(this.trainerListShowUndefeated != this.showUndefeated.selected()) {
                        this.showUndefeated.onPress();
                    }

                    this.trainerListShowUndefeated = null;
                }

                if(this.trainerListType != null) {
                    this.trainerTypeButton.setMessage(this.trainerListType);
                    this.trainerListType = null;
                }

                break;
            case TRAINER_INFO:
                if(this.trainerListShowUndefeated == null) {
                    this.trainerListShowUndefeated = this.showUndefeated.selected();
                }

                if(this.trainerListType == null) {
                    this.trainerListType = this.trainerTypeButton.getMessage();
                }

                if(this.showUndefeated.selected()) {
                    this.showUndefeated.onPress();
                }

                this.trainerInfo.visible = this.trainerInfo.active = true;
                this.trainerList.visible = this.trainerList.active = false;
                this.showUndefeated.active = false;
                this.trainerTypeButton.active = false;
                this.trainerTypeButton.setMessage(this.trainerInfo.getPageContent(this.trainerInfo.getPage()).title);
                break;
        }
    }

    public void tick() {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        var levelCap = playerState.getLevelCap();

        this.skinLocation = localPlayer.getSkinTextureLocation();
        this.displayName.setMessage(Component.literal(localPlayer.getDisplayName().getString()).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.WHITE));
        this.levelCapValue.setMessage(levelCap <= 100
            ? Component.literal(String.valueOf(levelCap)).withStyle(ChatFormatting.WHITE)
            : Component.literal("000").withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.WHITE));
            
        if(this.trainerList.active) {
            var totalDefeats = this.getTotalDefeats();
            this.totalDefeatsValue.setMessage(totalDefeats < 1000000
                ? Component.literal(String.valueOf(totalDefeats)).withStyle(ChatFormatting.WHITE)
                : Component.literal("1000000").withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.WHITE));

            this.nextPageButton.active = this.trainerList.getPage() < this.trainerList.getMaxPage();
            this.prevPageButton.active = this.trainerList.getPage() > 0;

            var trainerTypeStr = this.trainerTypeButton.getValue();
            var showUndefeated = this.showUndefeated.selected();
            var showAllTypes = trainerTypeStr.equals(ALL_TRAINER_TYPES_STR);

            if(!showAllTypes) {
                var trainerType = TrainerMobData.Type.valueOf(trainerTypeStr);
                this.trainerList.setTrainerType(trainerType);
            }

            this.trainerList.setShowAllTypes(showAllTypes);
            this.trainerList.setShowUndefeated(showUndefeated);
            this.trainerList.tick();
        } else if(this.trainerInfo.active) {
            var defeats = this.getDefeats(this.trainerInfo.getTrainerId());
            this.totalDefeatsValue.setMessage(defeats < 1000000
                ? Component.literal(String.valueOf(defeats)).withStyle(ChatFormatting.WHITE)
                : Component.literal("1000000").withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.WHITE));

            this.nextPageButton.active = this.trainerInfo.getPage() < this.trainerInfo.getMaxPage();
            this.prevPageButton.active = this.trainerInfo.getPage() > 0;
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blit(TRAINER_CARD_IMAGE_LOCATION, this.getX(), this.getY(), TRAINER_CARD_IMAGE_X, TRAINER_CARD_IMAGE_Y, TRAINER_CARD_IMAGE_W, TRAINER_CARD_IMAGE_H);

        if(this.skinLocation != null) {
            PlayerFaceRenderer.draw(guiGraphics, this.skinLocation, this.getX() + SKIN_X, this.getY() + SKIN_Y, SKIN_SIZE);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }

    private void onNextPage(Button button) {
        if(this.trainerList.active) {
            this.trainerList.setPage(this.trainerList.getPage() + 1);
        }

        if(this.trainerInfo.active) {
            this.trainerInfo.setPage(this.trainerInfo.getPage() + 1);
            this.trainerTypeButton.setMessage(this.trainerInfo.getPageContent(this.trainerInfo.getPage()).title);
        }
    }

    private void onPrevPage(Button button) {
        if(this.trainerList.active) {
            this.trainerList.setPage(this.trainerList.getPage() - 1);
        }

        if(this.trainerInfo.active) {
            this.trainerInfo.setPage(this.trainerInfo.getPage() - 1);
            this.trainerTypeButton.setMessage(this.trainerInfo.getPageContent(this.trainerInfo.getPage()).title);
        }
    }

    private long getTotalDefeats() {
        if(!this.showUndefeated.selected()) {
            return this.trainerList.getShowAllTypes()
                ? this.getDistinctDefeats()
                : this.getDistinctDefeats(this.trainerList.getTrainerType());
        }

        if(!this.trainerList.getShowAllTypes()) {
            return this.getTotalDefeats(this.trainerList.getTrainerType());
        }

        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        return playerState.getTrainerDefeatCount();
    }

    private long getTotalDefeats(TrainerMobData.Type type) {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        return playerState.getTypeDefeatCount(type);
    }

    private long getDistinctDefeats() {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        int count = 0;

        // TODO: consider optimization (cache value) if more trainer types get introduced
        for(var t : TrainerMobData.Type.values()) {
            count += playerState.getTypeDefeatCount(t, true);
        }

        return count;
    }

    private long getDistinctDefeats(TrainerMobData.Type type) {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        return playerState.getTypeDefeatCount(type, true);
    }

    private int getDefeats(String trainerId) {
        var localPlayer = (LocalPlayer)ModClient.get().getLocalPlayer().get();
        var playerState = PlayerState.get(localPlayer);
        return playerState.getTrainerDefeatCount(trainerId);
    }

    private static List<String> sortedTrainerIds() {
        var tdm = RCTMod.get().getTrainerManager();

        return tdm.getAllData().map(entry -> entry.getKey()).sorted((k1, k2) -> {
            var t1 = tdm.getData(k1);
            var t2 = tdm.getData(k2);
            var c = t1.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0)
                  - t2.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0);
            
            if(c == 0) {
                c = t1.getTeam().getDisplayName().compareTo(t2.getTeam().getDisplayName());

                if(c == 0) {
                    c = k1.compareTo(k2);
                }
            }

            return c;
        }).toList();
    }
}
