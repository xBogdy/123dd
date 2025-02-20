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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.algorithm.IAlgorithm;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerType;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.service.TrainerManager;
import com.gitlab.srcmc.rctmod.client.screens.widgets.text.MultiStyleStringWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;

public class TrainerListWidget extends TrainerDataWidget {
    public enum EntryState {UNKNOWN, HIDDEN_KEY, DISCOVERED, DISCOVERED_KEY}

    public static final int UPDATES_PER_TICK = 100;
    public static final int ENTRIES_PER_PAGE = 100;
    public static final int OBFUSCATION_INTERVAL_TICKS = 30;

    private class Entry {
        public final EntryState state;
        public final int trainerNr;
        public final String trainerId;
        public final MultiStyleStringWidget number;
        public final MultiStyleStringWidget name;
        public final MultiStyleStringWidget count;
        private boolean isObfuscated;
        private int currentTicks;

        public Entry(int trainerNr, String trainerId, EntryState state, MultiStyleStringWidget number, MultiStyleStringWidget name, MultiStyleStringWidget count) {
            this.state = state;
            this.trainerNr = trainerNr;
            this.trainerId = trainerId;
            this.number = number;
            this.name = name;
            this.count = count;
            this.isObfuscated = state == EntryState.DISCOVERED_KEY || state == EntryState.HIDDEN_KEY;
        }

        public void render(GuiGraphics guiGraphics, int x, int y, float f) {
            if(this.state == EntryState.DISCOVERED_KEY  && ticks != this.currentTicks && ticks % OBFUSCATION_INTERVAL_TICKS == 0) {
                this.isObfuscated = !this.isObfuscated;
                this.currentTicks = ticks;
            }

            if(this.isMouseOver(localX(x), localY(y))) {
                this.number.setStyle(1);
                this.name.setStyle(this.isObfuscated ? 3 : 1);
                this.count.setStyle(1);
                hovered = this;
            } else {
                this.number.setStyle(0);
                this.name.setStyle(this.isObfuscated ? 2 : 0);
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
            var hasUpdate = false;

            for(; this.i < trainerIds.size() && u < UPDATES_PER_TICK; this.i++, u++) {
                var trainerId = trainerIds.get(this.i);
                var trMob = this.tdm.getData(trainerId);

                if(showAllTypes || trMob.getType().equals(trainerType)) {
                    var count = this.playerState.getTrainerDefeatCount(trainerId);
                    var isNextKeyTrainer = playerState.isKeyTrainer(trainerId);

                    if(showUndefeated || isNextKeyTrainer || count > 0) {
                        p = this.c / ENTRIES_PER_PAGE;
                        r = this.c % ENTRIES_PER_PAGE;
                        this.y = r * this.h;
                        this.c++;

                        this.pages.computeIfAbsent(p, ArrayList::new).add(this.createEntry(
                            this.i + 1, trainerId,
                            count > 0 ? (isNextKeyTrainer ? EntryState.DISCOVERED_KEY : EntryState.DISCOVERED) : isNextKeyTrainer ? EntryState.HIDDEN_KEY : EntryState.UNKNOWN,
                            trMob, count, isNextKeyTrainer));

                        hasUpdate = true;
                    }
                }
            }

            if(!this.realtime && this.finished()) {
                TrainerListWidget.this.pages = pages;
                TrainerListWidget.this.maxPage = p;
                TrainerListWidget.this.page = Math.max(0, Math.min(p, TrainerListWidget.this.page));
                updateInnerHeight();
            } else if(this.realtime && hasUpdate) {
                TrainerListWidget.this.maxPage = p;
                updateInnerHeight();
            }
        }

        @Override
        public boolean finished() {
            return this.i >= TrainerListWidget.this.trainerIds.size();
        }

        private Entry createEntry(int trainerNr, String trainerId, EntryState entryState, TrainerMobData trMob, int defeatCount, boolean isKeyTrainer) {
            var name = entryState != EntryState.UNKNOWN ? trMob.getTrainerTeam().getName() : "???";
            var nameComponent = toComponent(name);
            var numberWidget = new MultiStyleStringWidget(TrainerListWidget.this, this.x, this.y, this.w, this.h, toComponent(String.format("%04d: ", trainerNr)), font)
                .addStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                .alignLeft();
            var nameWidget = new MultiStyleStringWidget(TrainerListWidget.this, (int)(this.x + this.w*0.18), this.y, (int)(this.w*0.7), this.h, nameComponent, font)
                .addStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                .addStyle(Style.EMPTY.withObfuscated(true))
                .addStyle(Style.EMPTY.withColor(ChatFormatting.RED).withObfuscated(true))
                .scrolling().alignLeft();
            var countWidget = new MultiStyleStringWidget(TrainerListWidget.this, this.x, this.y, this.w, this.h,
                    defeatCount > 9000 ? toComponent(" >9k") :
                    defeatCount > 999 ? toComponent(((defeatCount % 1000) != 0 ? " >" : " ") + (defeatCount/1000) + "k") :
                    toComponent(" " + defeatCount), font)
                .addStyle(Style.EMPTY.withColor(ChatFormatting.RED))
                .alignRight();

            return new Entry(trainerNr, trainerId, entryState, numberWidget, nameWidget, countWidget);
        }
    }

    public interface TrainerClickedConsumer {
        void accept(int trainerNr, String trainerId, EntryState entryState);
    }

    private int innerHeight, entryHeight;
    private List<String> trainerIds;
    private boolean showUndefeated, showAllTypes = true;
    private TrainerType trainerType = TrainerType.DEFAULT;
    private Map<Integer, List<Entry>> pages = new HashMap<>();
    private TrainerClickedConsumer trainerClickedHandler;
    private UpdateState updateState;
    private int page, maxPage;
    private Entry hovered, selected;
    private int ticks;
    
    public TrainerListWidget(int x, int y, int w, int h, Font font, List<String> trainerIds) {
        super(x, y, w, h, font);
        this.entryHeight = this.getHeight()/8;
        this.setTrainerIds(trainerIds);
    }

    public void setTrainerIds(Collection<String> trainerIds) {
        this.trainerIds = List.copyOf(trainerIds);
        this.pages = new HashMap<>();
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

    public TrainerType getTrainerType() {
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

    public void setTrainerType(TrainerType trainerType) {
        if(!this.trainerType.equals(trainerType)) {
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

        ++this.ticks;
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
