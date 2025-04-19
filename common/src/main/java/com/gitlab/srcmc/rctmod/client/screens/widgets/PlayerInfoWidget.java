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
package com.gitlab.srcmc.rctmod.client.screens.widgets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.spongepowered.include.com.google.common.base.Strings;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.Text;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.LangKeys;
import com.gitlab.srcmc.rctmod.client.screens.widgets.controls.CycleButton;
import com.gitlab.srcmc.rctmod.client.screens.widgets.text.AutoScaledStringWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PlayerInfoWidget extends AbstractWidget {
    public enum Display { TRAINER_LIST, TRAINER_INFO, LOADING }

    public static final ResourceLocation TRAINER_CARD_IMAGE_LOCATION = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/trainer_card.png");
    public static final int TRAINER_CARD_IMAGE_X = 0;
    public static final int TRAINER_CARD_IMAGE_Y = 0;
    public static final int TRAINER_CARD_IMAGE_W = 224;
    public static final int TRAINER_CARD_IMAGE_H = 128;

    public static final int SKIN_X = 13;
    public static final int SKIN_Y = 32;
    public static final int SKIN_SIZE = 72;

    public static final int DISPLAY_NAME_X = 8 + 8;
    public static final int DISPLAY_NAME_Y = 8;
    public static final int DISPLAY_NAME_W = 82 - 16;
    public static final int DISPLAY_NAME_H = 16;

    public static final int LEVEL_CAP_X = 8;
    public static final int LEVEL_CAP_Y = 104;
    public static final int LEVEL_CAP_W = 82;
    public static final int LEVEL_CAP_H = 16;
    public static final int LEVEL_CAP_PADDING = 4;

    public static final int TOTAL_DEFEATS_X = 96;
    public static final int TOTAL_DEFEATS_Y = 104;
    public static final int TOTAL_DEFEATS_W = 88;
    public static final int TOTAL_DEFEATS_H = 16;
    public static final int TOTAL_DEFEATS_PADDING = 4;

    public static final int TRAINER_LIST_X = 96;
    public static final int TRAINER_LIST_Y = 32;
    public static final int TRAINER_LIST_W = 112;
    public static final int TRAINER_LIST_H = 72;

    public static final int TYPE_BUTTON_X = 96;
    public static final int TYPE_BUTTON_Y = 8;
    public static final int TYPE_BUTTON_W = 100;
    public static final int TYPE_BUTTON_H = 16;

    public static final int CHECKBOX_X = 196;
    public static final int CHECKBOX_Y = 8;
    public static final int CHECKBOX_W = 20;
    public static final int CHECKBOX_H = 16;
    public static final List<String> CHECKBOX_VALUES = List.of(" ", "✔");

    public static final int NEXT_PAGE_BUTTON_X = 200;
    public static final int NEXT_PAGE_BUTTON_Y = 104;
    public static final int NEXT_PAGE_BUTTON_SIZE = 16;

    public static final int LOADING_W = TRAINER_LIST_W;
    public static final int LOADING_H = 8;
    public static final int LOADING_X = TRAINER_LIST_X;
    public static final int LOADING_Y = TRAINER_LIST_Y + TRAINER_LIST_H/2 - LOADING_H/2;

    private final TrainerType ALL_TRAINER_TYPES = new TrainerType(new Text().setTranslatable(LangKeys.TRAINER_TYPE_TITLE("all")));

    private StringWidget displayName;
    private StringWidget levelCapLabel;
    private StringWidget levelCapValue;
    private StringWidget totalDefeatsLabel;
    private StringWidget totalDefeatsValue;
    private StringWidget loadingLabel;

    private TrainerListWidget trainerList;
    private TrainerInfoWidget trainerInfo;
    private CycleButton<TrainerType> trainerTypeButton;
    private Button nextPageButton;
    private Button prevPageButton;
    private CycleButton<String> showUndefeated;

    private AbstractWidget[] renderableWidgets;
    private AbstractWidget[] renderableOnlies;

    private Boolean trainerListShowUndefeated;
    private Component trainerListType;

    private ResourceLocation skinLocation;
    private Font font;

    private List<TrainerType> trainerTypes;
    private String sid;

    private Display currentDisplay;
    private int loadingTick;

    public PlayerInfoWidget(int x, int y, int w, int h, Font font) {
        super(x, y, w, h, Component.empty());
        this.active = false;
        this.font = font;
        this.displayName = new AutoScaledStringWidget(x + DISPLAY_NAME_X, y + DISPLAY_NAME_Y, DISPLAY_NAME_W, DISPLAY_NAME_H, Component.empty(), this.font).alignCenter().fitting(true);
        this.levelCapLabel = new StringWidget(x + LEVEL_CAP_X + LEVEL_CAP_PADDING, y + LEVEL_CAP_Y + LEVEL_CAP_H/8, LEVEL_CAP_W, LEVEL_CAP_H, Component.translatable(LangKeys.GUI_TRAINER_CARD_LEVEL_CAP).withStyle(ChatFormatting.WHITE), this.font).alignLeft();
        this.levelCapValue = new StringWidget(x + LEVEL_CAP_X, y + LEVEL_CAP_Y + LEVEL_CAP_H/8, LEVEL_CAP_W - LEVEL_CAP_PADDING, LEVEL_CAP_H, Component.empty(), this.font).alignRight();
        this.totalDefeatsLabel = new StringWidget(x + TOTAL_DEFEATS_X + TOTAL_DEFEATS_PADDING, y + TOTAL_DEFEATS_Y + TOTAL_DEFEATS_H/8, TOTAL_DEFEATS_W, TOTAL_DEFEATS_H, Component.translatable(LangKeys.GUI_TRAINER_CARD_TOTAL).withStyle(ChatFormatting.WHITE), this.font).alignLeft();
        this.totalDefeatsValue = new StringWidget(x + TOTAL_DEFEATS_X, y + TOTAL_DEFEATS_Y + TOTAL_DEFEATS_H/8, TOTAL_DEFEATS_W - TOTAL_DEFEATS_PADDING, TOTAL_DEFEATS_H, Component.empty(), this.font).alignRight();
        this.loadingLabel = new AutoScaledStringWidget(x + LOADING_X, y + LOADING_Y, LOADING_W, LOADING_H, Component.empty(), this.font).alignCenter().scaled(0.65f);
        this.trainerList = new TrainerListWidget(x + TRAINER_LIST_X, y + TRAINER_LIST_Y, TRAINER_LIST_W, TRAINER_LIST_H, font, this.sortedTrainerIds());
        this.trainerInfo = new TrainerInfoWidget(x + TRAINER_LIST_X, y + TRAINER_LIST_Y, TRAINER_LIST_W, TRAINER_LIST_H, font);
        this.trainerTypes = new ArrayList<>();
        this.trainerTypes.add(ALL_TRAINER_TYPES);

        this.showUndefeated = CycleButton.create(
            t -> Component.literal(t), CHECKBOX_VALUES, 1,
            x + CHECKBOX_X, y + CHECKBOX_Y, CHECKBOX_W, CHECKBOX_H, true);

        this.trainerTypeButton = CycleButton.create(
            t -> t.name().asComponent(), this.trainerTypes, 0,
            x + TYPE_BUTTON_X, y + TYPE_BUTTON_Y, TYPE_BUTTON_W, TYPE_BUTTON_H, false);
        
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
            this.loadingLabel,
        };

        this.trainerList.setOnTrainerClicked((trainerNr, trainerId, entryState) -> {
            this.trainerInfo.initTrainerInfo(trainerNr, trainerId, entryState);
            this.trainerInfo.setPage(0);
            this.setDisplay(Display.TRAINER_INFO);
        });

        this.trainerInfo.setOnBackClicked(i -> this.setDisplay(Display.TRAINER_LIST));
        this.setDisplay(Display.LOADING);
    }

    private void resetTrainerTypes() {
        this.trainerTypes.clear();
        this.trainerTypes.add(ALL_TRAINER_TYPES);
        this.trainerTypeButton.setValue(0);
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
                if(this.currentDisplay == Display.LOADING) {
                    this.resetTrainerTypes();
                    this.updateTrainerTypes(this.sid);
                    this.trainerList.setTrainerIds(this.sortedTrainerIds());
                }

                this.trainerInfo.visible = this.trainerInfo.active = false;
                this.trainerList.visible = this.trainerList.active = true;
                this.showUndefeated.active = true;
                this.trainerTypeButton.active = true;
                this.loadingLabel.setMessage(Component.empty());

                if(this.trainerListShowUndefeated != null) {
                    if(this.trainerListShowUndefeated != this.isShowUndefeatedSelected()) {
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
                    this.trainerListShowUndefeated = this.isShowUndefeatedSelected();
                }

                if(this.trainerListType == null) {
                    this.trainerListType = this.trainerTypeButton.getMessage();
                }

                if(this.isShowUndefeatedSelected()) {
                    this.showUndefeated.onPress();
                }

                this.trainerInfo.visible = this.trainerInfo.active = true;
                this.trainerList.visible = this.trainerList.active = false;
                this.prevPageButton.active = false;
                this.nextPageButton.active = false;
                this.showUndefeated.active = false;
                this.trainerTypeButton.active = false;
                this.trainerTypeButton.setMessage(this.trainerInfo.getPageContent(this.trainerInfo.getPage()).title);
                this.loadingLabel.setMessage(Component.empty());

                break;
            case LOADING:
                if(this.isShowUndefeatedSelected()) {
                    this.showUndefeated.onPress();
                }

                this.trainerInfo.visible = this.trainerInfo.active = false;
                this.trainerList.visible = this.trainerList.active = true;
                this.trainerListShowUndefeated = true;
                this.showUndefeated.active = false;
                this.trainerTypeButton.active = false;
                this.trainerList.setTrainerIds(List.of());
                this.trainerList.setPage(0);
                this.resetTrainerTypes();

                break;
        }

        this.currentDisplay = display;
    }

    private void updateLoading() {
        this.loadingLabel.setMessage(Component.literal(Component.translatable(LangKeys.GUI_TRAINER_CARD_LOADING).getString() + Strings.repeat(".", (this.loadingTick/15) % 6)).withStyle(ChatFormatting.GREEN));
    }

    public void tick() {
        var mc = Minecraft.getInstance();
        var ps = PlayerState.get(mc.player);
        var tm = RCTMod.getInstance().getTrainerManager();
        var levelCap = this.currentDisplay == Display.LOADING ? 0 : ps.getLevelCap();

        this.sid = ps.getCurrentSeries();
        this.skinLocation = mc.player.getSkin().texture();
        this.displayName.setMessage(Component.literal(mc.player.getDisplayName().getString()).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.WHITE));
        this.levelCapValue.setMessage(levelCap <= 100
            ? Component.literal(String.valueOf(levelCap)).withStyle(ChatFormatting.WHITE)
            : Component.literal("000").withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.WHITE));

        if(tm.isLoading() || ps.isLoading()) {
            if(this.currentDisplay != Display.LOADING) {
                this.setDisplay(Display.LOADING);
                this.loadingTick = 0;
            }

            this.updateLoading();
            this.loadingTick++;
        } else if(this.currentDisplay == Display.LOADING) {
            this.setDisplay(Display.TRAINER_LIST);
        }

        if(this.trainerList.active) {
            var totalDefeats = this.currentDisplay == Display.LOADING ? 0 : this.getTotalDefeats();
            this.totalDefeatsValue.setMessage(totalDefeats < 1000000
                ? Component.literal(String.valueOf(totalDefeats)).withStyle(ChatFormatting.WHITE)
                : Component.literal("1000000").withStyle(ChatFormatting.OBFUSCATED).withStyle(ChatFormatting.WHITE));

            this.nextPageButton.active = this.trainerList.getPage() < this.trainerList.getMaxPage();
            this.prevPageButton.active = this.trainerList.getPage() > 0;

            var trainerTypeIndex = this.trainerTypeButton.getIndex();
            var showUndefeated = this.isShowUndefeatedSelected();
            var showAllTypes = trainerTypeIndex == 0;

            this.trainerList.setTrainerType(this.trainerTypeButton.getValue());
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
            this.trainerInfo.tick();
        }
    }

    private void updateTrainerTypes(String sid) {
        sid = sid == null ? "" : sid;

        var sm = RCTMod.getInstance().getSeriesManager();
        var tm = RCTMod.getInstance().getTrainerManager();
        var it = sm.getGraph(sid).stream().iterator();
        var open = new LinkedList<>(TrainerType.values());

        while(it.hasNext() && !open.isEmpty()) {
            var tt = tm.getData(it.next().id()).getType();
            var it2 = open.iterator();

            while(it2.hasNext()) {
                var t = it2.next();

                if(tt.equals(t)) {
                    this.trainerTypeButton.addValue(t);
                    it2.remove();
                }
            }
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

    private boolean isShowUndefeatedSelected() {
        return this.showUndefeated.getValue().equals(CHECKBOX_VALUES.get(1));
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
        if(!this.isShowUndefeatedSelected()) {
            return this.trainerList.getShowAllTypes()
                ? this.getDistinctDefeats()
                : this.getDistinctDefeats(this.trainerList.getTrainerType());
        }

        if(!this.trainerList.getShowAllTypes()) {
            return this.getTotalDefeats(this.trainerList.getTrainerType());
        }

        var mc = Minecraft.getInstance();
        var playerState = PlayerState.get(mc.player);
        return playerState.getTrainerDefeatCount();
    }

    private long getTotalDefeats(TrainerType type) {
        var mc = Minecraft.getInstance();
        var playerState = PlayerState.get(mc.player);
        return playerState.getTypeDefeatCount(type);
    }

    private long getDistinctDefeats() {
        var mc = Minecraft.getInstance();
        var playerState = PlayerState.get(mc.player);
        int count = 0;

        // TODO: consider optimization (cache value) if more trainer types get introduced
        for(var t : TrainerType.values()) {
            count += playerState.getTypeDefeatCount(t, true);
        }

        return count;
    }

    private long getDistinctDefeats(TrainerType type) {
        var mc = Minecraft.getInstance();
        var playerState = PlayerState.get(mc.player);
        return playerState.getTypeDefeatCount(type, true);
    }

    private int getDefeats(String trainerId) {
        var mc = Minecraft.getInstance();
        var playerState = PlayerState.get(mc.player);
        return playerState.getTrainerDefeatCount(trainerId);
    }

    private List<String> sortedTrainerIds() {
        var mc = Minecraft.getInstance();
        var playerState = PlayerState.get(mc.player);
        var tdm = RCTMod.getInstance().getTrainerManager();

        return tdm.getAllData(playerState.getCurrentSeries()).map(entry -> entry.getKey()).sorted((k1, k2) -> {
            var t1 = tdm.getData(k1);
            var t2 = tdm.getData(k2);
            var c = t1.getTrainerTeam().getTeam().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0)
                  - t2.getTrainerTeam().getTeam().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0);
            
            if(c == 0) {
                c = t1.getTrainerTeam().getName().compareTo(t2.getTrainerTeam().getName());

                if(c == 0) {
                    c = k1.compareTo(k2);
                }
            }

            return c;
        }).toList();
    }
}
