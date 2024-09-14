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
import java.util.function.Consumer;

import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.client.screens.widgets.TrainerListWidget.EntryState;
import com.gitlab.srcmc.rctmod.client.screens.widgets.text.MultiStyleStringWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class TrainerInfoWidget extends TrainerDataWidget {
    private static final int MAX_NAME_LENGTH = 20;

    public class PageContent {
        public final Component title;
        public final List<Renderable> renderables = new ArrayList<>();

        private PageContent(String title, Renderable... renderables) {
            this.title = Component.literal(title);

            for(var r : renderables) {
                this.renderables.add(r);
            }
        }
    }

    private Consumer<Integer> backClickedHandler;
    private String trainerId;
    private int page;

    private TrainerMobData trainer;
    private EntryState entryState;
    private int w, h, y;

    private List<PageContent> contents = new ArrayList<>();
    private HoverElement<MultiStyleStringWidget> back;
    private StringWidget number, name, aka, identity;

    public TrainerInfoWidget(int x, int y, int w, int h, Font font) {
        super(x, y, w, h, font);
    }

    public void initTrainerInfo(int trainerNr, String trainerId, EntryState entryState) {
        this.trainer = RCTMod.get().getTrainerManager().getData(trainerId);
        this.trainerId = trainerId;
        this.entryState = entryState;

        this.w = (int)((this.getWidth() - this.totalInnerPadding())/INNER_SCALE);
        this.h = this.getHeight() / 6;
        this.y = 0;

        var displayName = entryState == EntryState.UNKNOWN ? "???" : trainer.getTeam().getDisplayName();
        var identity = entryState == EntryState.UNKNOWN ? "???" : trainer.getTeam().getIdentity();

        if(displayName.length() > MAX_NAME_LENGTH) {
            displayName = displayName.substring(0, MAX_NAME_LENGTH - 3) + "...";
        }

        if(identity.length() > MAX_NAME_LENGTH) {
            identity = identity.substring(0, MAX_NAME_LENGTH - 3) + "...";
        }

        this.back = new HoverElement<>(
            new MultiStyleStringWidget((int)(this.w*0.92), this.y, (int)(this.w*0.08), h, toComponent("[X]"), this.font).addStyle(Style.EMPTY.withColor(ChatFormatting.RED)).alignRight(),
            msw -> msw.setStyle(1), msw -> msw.setStyle(0));

        this.number = new StringWidget(0, this.y, this.w, this.h, toComponent(String.format("%04d: ", trainerNr)), this.font).alignLeft();
        this.name = new StringWidget((int)(this.w*0.18), this.y, (int)(this.w*0.72), this.h, entryState == EntryState.HIDDEN ? toComponent(displayName).withStyle(ChatFormatting.OBFUSCATED) : toComponent(displayName).withStyle(ChatFormatting.UNDERLINE), this.font).alignLeft();

        if(entryState != EntryState.DISCOVERED || identity.equals(displayName)) {
            this.aka = this.identity = null;
        } else {
            this.aka = new StringWidget(0, this.y += this.h, this.w, this.h, toComponent("aka"), this.font).alignLeft();
            this.identity = identity.equals(displayName) ? null : new StringWidget((int)(this.w*0.18), this.y, this.w, this.h, entryState == EntryState.HIDDEN ? toComponent(identity).withStyle(ChatFormatting.OBFUSCATED) : toComponent(identity), this.font).alignLeft();
        }

        this.back.element.active = true;
        this.contents.clear();
        this.contents.add(initOverviewPage());

        if(entryState != EntryState.UNKNOWN) {
            this.contents.add(initSpawningPage());
        }

        if(entryState == EntryState.DISCOVERED) {
            this.contents.add(initTeamPage());
            this.contents.add(initLootPage());
        }
    }

    private PageContent initPage(String title) {
        var pc = new PageContent(title, this.number, this.name, this.back);

        if(this.identity != null) {
            pc.renderables.add(this.aka);
            pc.renderables.add(this.identity);
        }

        return pc;
    }

    private PageContent initOverviewPage() {
        var pc = initPage("Overview");

        pc.renderables.add(new StringWidget(8, this.y += this.h, this.w, this.h, toComponent("Type: "), this.font).alignLeft());
        pc.renderables.add(new StringWidget(8, this.y, (int)(this.w*0.9), this.h, toComponent(this.trainer.getType().name()), this.font).alignRight());

        pc.renderables.add(new StringWidget(8, this.y += this.h, this.w, this.h, toComponent("Rew. Level Cap: "), this.font).alignLeft());
        pc.renderables.add(new StringWidget(8, this.y, (int)(this.w*0.9), this.h, toComponent(this.trainer.getRewardLevelCap()), this.font).alignRight());

        pc.renderables.add(new StringWidget(8, this.y += this.h , this.w, this.h, toComponent("Req. Level Cap: "), this.font).alignLeft());
        pc.renderables.add(new StringWidget(8, this.y, (int)(this.w*0.9), this.h, toComponent(this.trainer.getRequiredLevelCap()), this.font).alignRight());

        for(var type : TrainerMobData.Type.values()) {
            var c = trainer.getRequiredDefeats(type);

            if(c > 0) {
                pc.renderables.add(new StringWidget(8, this.y += this.h , this.w, this.h, toComponent(String.format("Req. %s: ", type.name())), this.font).alignLeft());
                pc.renderables.add(new StringWidget(8, this.y, (int)(this.w*0.9), this.h, toComponent(c), this.font).alignRight());
            }
        }

        return pc;
    }

    private PageContent initSpawningPage() {
        var pc = initPage("Spawning");
        return pc;
    }

    private PageContent initTeamPage() {
        var pc = initPage("Team");
        return pc;
    }

    private PageContent initLootPage() {
        var pc = initPage("Loot");
        return pc;
    }

    public String getTrainerId() {
        return this.trainerId;
    }

    public PageContent getPageContent(int page) {
        return this.contents.get(page);
    }

    @Override
    protected void renderPage(GuiGraphics guiGraphics, int x, int y, float f) {        
        for(var r : this.getPageContent(this.getPage()).renderables) {
            r.render(guiGraphics, x, y, f);
        }
    }

    @Override
    public int getPage() {
        return this.page;
    }

    @Override
    public int getMaxPage() {
        return this.contents.size() - 1;
    }

    @Override
    public void setPage(int page) {
        this.page = Math.max(0, Math.min(this.contents.size() - 1, page));
    }

    public void setOnBackClicked(Consumer<Integer> backClickedHandler) {
        this.backClickedHandler = backClickedHandler;
    }

    @Override
    public boolean mouseClicked(double x, double y, int i) {
        if(super.mouseClicked(x, y, i)) {
            if(this.backClickedHandler != null) {
                x = localX(x);
                y = localY(y);

                if(this.back.element.mouseClicked(x, y, i)) {
                    this.backClickedHandler.accept(i);
                }
            }

            return true;
        }

        return false;
    }
}
