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
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.algorithm.IAlgorithm;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.client.screens.widgets.text.MultiStyleStringWidget;
import com.gitlab.srcmc.rctmod.client.screens.widgets.text.TextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;

public class TrainerListWidget extends TrainerDataWidget {
    public enum EntryState {UNKNOWN, HIDDEN, DISCOVERED}

    private static final int UPDATES_PER_TICK = 100;
    private static final int ENTRIES_PER_PAGE = 100;
    private static final int MAX_NAME_LENGTH = 20;

    private class Entry {
        public final EntryState state;
        public final int trainerNr;
        public final String trainerId;
        public final MultiStyleStringWidget number;
        public final MultiStyleStringWidget name;
        public final MultiStyleStringWidget count;

        public Entry(int trainerNr, String trainerId, EntryState state, MultiStyleStringWidget number, MultiStyleStringWidget name, MultiStyleStringWidget count) {
            this.state = state;
            this.trainerNr = trainerNr;
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
                hovered = this;
            } else {
                this.number.setStyle(0);
                this.name.setStyle(0);
                this.count.setStyle(0);
            }

            this.number.render(guiGraphics, x, y, f);
            this.name.render(guiGraphics, x, y, f);
            this.count.render(guiGraphics, x, y, f);
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
            var mc = Minecraft.getInstance();
            this.realtime = pages == TrainerListWidget.this.pages;
            this.pages = pages;
            this.playerState = PlayerState.get(mc.player);
            this.tdm = RCTMod.getInstance().getTrainerManager();
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
                    var isNextKeyTrainer = count == 0 && playerState.isKeyTrainer(trMob) && playerState.canBattle(trMob);

                    if(showUndefeated || isNextKeyTrainer || count > 0) {
                        p = this.c / ENTRIES_PER_PAGE;
                        r = this.c % ENTRIES_PER_PAGE;
                        this.y = r * this.h;
                        this.c++;

                        this.pages.computeIfAbsent(p, ArrayList::new).add(this.createEntry(
                            this.i + 1, trainerId,
                            count > 0 ? EntryState.DISCOVERED : isNextKeyTrainer ? EntryState.HIDDEN : EntryState.UNKNOWN,
                            trMob, count, isNextKeyTrainer));

                        if(this.realtime) {
                            TrainerListWidget.this.maxPage = p;
                            updateInnerHeight();
                        }
                    }
                }
            }

            if(!realtime && this.finished()) {
                TrainerListWidget.this.pages = pages;
                TrainerListWidget.this.maxPage = p;
                TrainerListWidget.this.page = Math.max(0, Math.min(p, TrainerListWidget.this.page));
                updateInnerHeight();
            }
        }

        @Override
        public boolean finished() {
            return this.i >= TrainerListWidget.this.trainerIds.size();
        }

        private Entry createEntry(int trainerNr, String trainerId, EntryState entryState, TrainerMobData trMob, int defeatCount, boolean isKeyTrainer) {
            var name = TextUtils.trim(isKeyTrainer || defeatCount > 0 ? trMob.getTrainerTeam().getName() : "???", MAX_NAME_LENGTH);
            var nameComponent = (defeatCount == 0 && isKeyTrainer) ? toComponent(name).withStyle(ChatFormatting.OBFUSCATED) : toComponent(name);
            var numberWidget = new MultiStyleStringWidget(this.x, this.y, this.w, this.h, toComponent(String.format("%04d: ", trainerNr)), font).addStyle(Style.EMPTY.withColor(ChatFormatting.RED)).alignLeft();
            var nameWidget = new MultiStyleStringWidget((int)(this.x + this.w*0.18), this.y, (int)(this.w*0.62), this.h, nameComponent, font).addStyle(Style.EMPTY.withColor(ChatFormatting.RED)).alignLeft();
            var countWidget = new MultiStyleStringWidget(this.x, this.y, this.w, this.h,
                    defeatCount > 9000 ? toComponent(" >9k") :
                    defeatCount > 999 ? toComponent(((defeatCount % 1000) != 0 ? " >" : " ") + (defeatCount/1000) + "k") :
                    toComponent(" " + defeatCount), font)
                .addStyle(Style.EMPTY.withColor(ChatFormatting.RED)).alignRight();

            return new Entry(trainerNr, trainerId, entryState, numberWidget, nameWidget, countWidget);
        }
    }

    public interface TrainerClickedConsumer {
        void accept(int trainerNr, String trainerId, EntryState entryState);
    }

    private int innerHeight, entryHeight;
    private List<String> trainerIds;
    private boolean showUndefeated, showAllTypes = true;
    private TrainerMobData.Type trainerType;
    private Map<Integer, List<Entry>> pages = new HashMap<>();
    private TrainerClickedConsumer trainerClickedHandler;
    private UpdateState updateState;
    private int page, maxPage;
    private Entry hovered, selected;
    
    public TrainerListWidget(int x, int y, int w, int h, Font font, List<String> trainerIds) {
        super(x, y, w, h, font);
        this.trainerIds = new ArrayList<>(trainerIds);
        this.entryHeight = this.getHeight()/8;
        this.updateState = new UpdateState(this.pages);
        this.updateInnerHeight();
    }

    public int size() {
        return this.pages.values().stream().map(p -> p.size()).reduce(0, (s1, s2) -> s1 + s2);
    }

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public int getMaxPage() {
        return this.maxPage;
    }

    @Override
    public void setPage(int page) {
        var nextPage = Math.max(0, Math.min(this.maxPage, page));

        if(nextPage != this.page) {
            this.page = nextPage;
            this.setScrollAmount(0);
            this.updateInnerHeight();
        }
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

    public void setOnTrainerClicked(TrainerClickedConsumer trainerClickedHandler) {
        this.trainerClickedHandler = trainerClickedHandler;
    }

    public void tick() {
        if(this.updateState.finished()) {
            this.updateState = new UpdateState();
        } else {
            this.updateState.tick();
        }
    }

    @Override
    protected int getInnerHeight() {
        return (int)(this.innerHeight*INNER_SCALE);
    }

    private void updateInnerHeight() {
        this.innerHeight = Math.max(
            this.getHeight() - this.totalInnerPadding(),
            this.pages.getOrDefault(this.page, List.of()).size()*this.entryHeight);
    }

    @Override
    protected void renderPage(GuiGraphics guiGraphics, int x, int y, float f) {
        this.hovered = null;

        for(var entry : this.pages.getOrDefault(this.page, List.of())) {
            entry.render(guiGraphics, x, y, f);
        }

        if(this.hovered != this.selected) {
            this.selected = this.hovered;
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int i) {
        if(super.mouseClicked(x, y, i)) {
            if(this.trainerClickedHandler != null && this.selected != null) {
                this.trainerClickedHandler.accept(this.selected.trainerNr, this.selected.trainerId, this.selected.state);
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
}
