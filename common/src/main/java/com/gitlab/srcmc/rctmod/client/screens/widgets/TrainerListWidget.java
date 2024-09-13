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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.algorithm.IAlgorithm;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.client.ModClient;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public class TrainerListWidget extends AbstractScrollWidget {
    public static final float INNER_SCALE = 0.65f;
    private static final int UPDATES_PER_TICK = 100;
    private static final int ENTRIES_PER_PAGE = 100;
    private static final int MAX_NAME_LENGTH = 20;

    private class Entry {
        public final String trainerId;
        public final MultiStyleStringWidget number;
        public final MultiStyleStringWidget name;
        public final MultiStyleStringWidget count;

        public Entry(String trainerId, MultiStyleStringWidget number, MultiStyleStringWidget name, MultiStyleStringWidget count) {
            this.trainerId = trainerId;
            this.number = number;
            this.name = name;
            this.count = count;
            this.number.active = true;
            this.name.active = true;
            this.count.active = true;
        }

        public void render(GuiGraphics guiGraphics, int x, int y, float f) {
            if(this.isMouseOver(localX(x), localY(y))) {
                this.number.setStyle(1);
                this.name.setStyle(1);
                this.count.setStyle(1);
            } else {
                this.number.setStyle(0);
                this.name.setStyle(0);
                this.count.setStyle(0);
            }

            this.number.render(guiGraphics, x, y, f);
            this.name.render(guiGraphics, x, y, f);
            this.count.render(guiGraphics, x, y, f);
        }

        public boolean mouseClicked(double localX, double localY, int i) {
            return this.number.mouseClicked(localX, localY, i)
                || this.name.mouseClicked(localX, localY, i)
                || this.count.mouseClicked(localX, localY, i);
        }

        public boolean isMouseOver(double localX, double localY) {
            return this.number.isMouseOver(localX, localY)
                || this.name.isMouseOver(localX, localY)
                || this.count.isMouseOver(localX, localY);
        }
    }

    private class UpdateState implements IAlgorithm {
        public final Map<Integer, List<Entry>> pages;
        public final PlayerState playerState;
        public int i, c, x, y, w, h;
        public TrainerManager tdm;
        private boolean realtime;

        public UpdateState() {
            this(new HashMap<>());
        }

        private UpdateState(Map<Integer, List<Entry>> pages) {
            this.realtime = pages == TrainerListWidget.this.pages;
            this.pages = pages;
            this.playerState = PlayerState.get(ModClient.get().getLocalPlayer().get());
            this.tdm = RCTMod.get().getTrainerManager();
            this.w = (int)((getWidth() - totalInnerPadding())/INNER_SCALE);
            this.h = TrainerListWidget.this.entryHeight;
        }

        @Override
        public void tick() {
            int u = 0, p = 0, r = 0;

            for(; this.i < trainerIds.size() && u < UPDATES_PER_TICK; this.i++, u++) {
                var trainerId = trainerIds.get(this.i);

                if(showAllTypes || this.tdm.getData(trainerId).getType() == trainerType) {
                    var count = this.playerState.getTrainerDefeatCount(trainerId);
                    var trMob = this.tdm.getData(trainerId);
                    var isKeyTrainer = this.playerState.getLevelCap() < trMob.getRewardLevelCap()
                        && this.playerState.getLevelCap() >= trMob.getRequiredLevelCap();

                    if(showUndefeated || isKeyTrainer || count > 0) {
                        p = this.c / ENTRIES_PER_PAGE;
                        r = this.c % ENTRIES_PER_PAGE;
                        this.y = r * this.h;
                        this.c++;
                        this.pages.computeIfAbsent(p, ArrayList::new).add(this.createEntry(trainerId, trMob, count, isKeyTrainer));

                        if(this.realtime) {
                            TrainerListWidget.this.maxPage = p;
                        }
                    }
                }
            }

            if(!realtime && this.finished()) {
                TrainerListWidget.this.pages = pages;
                TrainerListWidget.this.maxPage = p;
                TrainerListWidget.this.page = Math.max(0, Math.min(p, TrainerListWidget.this.page));
            }
        }

        @Override
        public boolean finished() {
            return this.i >= TrainerListWidget.this.trainerIds.size();
        }

        private Entry createEntry(String trainerId, TrainerMobData trMob, int defeatCount, boolean isKeyTrainer) {
            var name = isKeyTrainer || defeatCount > 0 ? trMob.getTeam().getDisplayName() : "???";

            if(name.length() > MAX_NAME_LENGTH) {
                name = name.substring(0, MAX_NAME_LENGTH - 3) + "...";
            }
            
            var nameComponent = isKeyTrainer ? toComponent(name).withStyle(ChatFormatting.OBFUSCATED) : toComponent(name);
            var numberWidget = new MultiStyleStringWidget(this.x, this.y, this.w, this.h, toComponent(String.format("%04d: ", this.i + 1)), font).addStyle(Style.EMPTY.withColor(ChatFormatting.RED)).alignLeft();
            var nameWidget = new MultiStyleStringWidget((int)(this.x + this.w*0.18), this.y, (int)(this.w*0.62), this.h, nameComponent, font).addStyle(Style.EMPTY.withColor(ChatFormatting.RED)).alignLeft();
            var countWidget = new MultiStyleStringWidget(this.x, this.y, this.w, this.h,
                    defeatCount > 9000 ? toComponent(" >9k") :
                    defeatCount > 999 ? toComponent(((defeatCount % 1000) != 0 ? " >" : " ") + (defeatCount/1000) + "k") :
                    toComponent(" " + defeatCount), font)
                .addStyle(Style.EMPTY.withColor(ChatFormatting.RED)).alignRight();

            return new Entry(trainerId, numberWidget, nameWidget, countWidget);
        }
    }

    private Font font;
    private int innerHeight, entryHeight;
    private List<String> trainerIds;
    private boolean showUndefeated, showAllTypes = true;
    private TrainerMobData.Type trainerType;
    private Map<Integer, List<Entry>> pages = new HashMap<>();
    private UpdateState updateState;
    private int page, maxPage;
    
    public TrainerListWidget(int x, int y, int w, int h, Font font, List<String> trainerIds) {
        super(x, y, w, h, Component.empty());
        this.font = font;
        this.trainerIds = new ArrayList<>(trainerIds);
        this.innerHeight = this.getHeight() - this.totalInnerPadding();
        this.entryHeight = this.getHeight()/8;
        this.updateState = new UpdateState(this.pages);
    }

    public void tick() {
        if(this.updateState.finished()) {
            this.updateState = new UpdateState();
        } else {
            this.updateState.tick();
        }

        this.innerHeight = Math.max(
            getHeight() - totalInnerPadding(),
            this.pages.getOrDefault(this.page, List.of()).size()*this.entryHeight);
    }

    private void renderEntries(GuiGraphics guiGraphics, int x, int y, float f) {
        for(var entry : this.pages.getOrDefault(this.page, List.of())) {
            entry.render(guiGraphics, x, y, f);
        }
    }

    public int getPage() {
        return this.page;
    }

    public int getMaxPage() {
        return this.maxPage;
    }

    public boolean getShowUndefeated() {
        return this.showUndefeated;
    }

    public TrainerMobData.Type getTrainerType() {
        return this.trainerType;
    }

    public boolean getShowAllTypes() {
        return this.showAllTypes;
    }

    public void setPage(int page) {
        var nextPage = Math.max(0, Math.min(this.maxPage, page));

        if(nextPage != this.page) {
            this.page = nextPage;
            this.setScrollAmount(0);
        }
    }

    public void setShowUndefeated(boolean showUndefeated) {
        if(this.showUndefeated != showUndefeated) {
            this.showUndefeated = showUndefeated;
            this.updateEntries();
        }
    }

    public void setTrainerType(TrainerMobData.Type trainerType) {
        if(this.trainerType != trainerType) {
            this.trainerType = trainerType;
            this.updateEntries();
        }
    }

    public void setShowAllTypes(boolean showAllTypes) {
        if(this.showAllTypes != showAllTypes) {
            this.showAllTypes = showAllTypes;
            this.updateEntries();
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int i) {
        if(super.mouseClicked(x, y, i)) {
            x = localX(x);
            y = localY(y);

            for(var entry : this.pages.getOrDefault(this.page, List.of())) {
                if(entry.mouseClicked(x, y, i)) {
                    ModCommon.LOG.info(String.format("CLICKED AT (%.2f, %.2f): %s", x, y, entry.trainerId));
                    break;
                }
            }

            return true;
        }

        return false;
    }

    private void updateEntries() {
        this.setScrollAmount(0);
        this.pages.clear();
        this.page = this.maxPage = 0;
        this.updateState = new UpdateState(this.pages);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        if(this.scrollbarVisible()) {
           super.renderBackground(guiGraphics);
        } else {
           this.renderBorder(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight());
           this.renderFullScrollBar(guiGraphics);
        }
    }
    
    private int getContentHeight() {
        return this.getInnerHeight() + 4;
    }

    private int getScrollBarHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
    }

    private void renderFullScrollBar(GuiGraphics guiGraphics) {
        int i = this.getScrollBarHeight();
        int j = this.getX() + this.width;
        int k = this.getX() + this.width + 8;
        int l = this.getY() + this.height - i;
        int m = l + i;
        guiGraphics.fill(j, l, k, m, -8355712);
        guiGraphics.fill(j, l, k - 1, m - 1, -4144960);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int x, int y, float f) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) (this.getX() + this.innerPadding()), (float) (this.getY() + this.innerPadding()), 0.0F);
        guiGraphics.pose().scale(INNER_SCALE, INNER_SCALE, 1f);
        this.renderEntries(guiGraphics, x, y, f);
        guiGraphics.pose().popPose();
    }

    protected double localX(double x) {
        return (x - this.getX()) / INNER_SCALE - this.innerPadding();
    }

    protected double localY(double y) {
        return (y - this.getY() + this.scrollAmount()) / INNER_SCALE - this.innerPadding();
    }

    @Override
    protected int getInnerHeight() {
        return (int)(this.innerHeight*INNER_SCALE);
    }

    @Override
    protected double scrollRate() {
        return 9.0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }

    private static<T> MutableComponent toComponent(T value) {
        return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.GREEN);
    }
}
