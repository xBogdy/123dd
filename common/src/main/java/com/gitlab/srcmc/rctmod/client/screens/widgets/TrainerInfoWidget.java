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
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.gitlab.srcmc.rctapi.api.util.Locations;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.pack.TrainerMobData;
import com.gitlab.srcmc.rctmod.client.screens.widgets.TrainerListWidget.EntryState;
import com.gitlab.srcmc.rctmod.client.screens.widgets.text.MultiStyleStringWidget;
import com.gitlab.srcmc.rctmod.client.screens.widgets.text.TextUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class TrainerInfoWidget extends TrainerDataWidget {
    private static final int MAX_SPECIES_LENGTH = 28;
    private static final int MAX_BIOME_LENGTH = 24;

    public class PageContent {
        public final Component title;
        public final List<Renderable> renderables = new ArrayList<>();
        protected int height;

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

    private EntryState entryState;
    private TrainerMobData trainer;
    private int innerHeight;
    private int w, h, y;

    private List<PageContent> contents = new ArrayList<>();
    private HoverElement<MultiStyleStringWidget> back;
    private StringWidget number, name, aka, identity;
    private boolean obfuscated;
    private int ticks;

    public TrainerInfoWidget(int x, int y, int w, int h, Font font) {
        super(x, y, w, h, font);
    }

    public void tick() {
        if(this.entryState == EntryState.DISCOVERED_KEY && this.name != null && this.ticks % TrainerListWidget.OBFUSCATION_INTERVAL_TICKS == 0) {
            var msg = this.name.getMessage().plainCopy().withStyle(ChatFormatting.GREEN);
            this.obfuscated = !this.obfuscated;

            if(this.obfuscated) {
                msg = msg.withStyle(ChatFormatting.OBFUSCATED);
            }

            this.name.setMessage(msg);

            if(this.identity != null) {
                msg = this.identity.getMessage().plainCopy().withStyle(ChatFormatting.GREEN);

                if(this.obfuscated) {
                    msg = msg.withStyle(ChatFormatting.OBFUSCATED);
                }

                this.identity.setMessage(msg);
            }
        }

        ++this.ticks;
    }

    public void initTrainerInfo(int trainerNr, String trainerId, EntryState entryState) {
        this.trainer = RCTMod.getInstance().getTrainerManager().getData(trainerId);
        this.trainerId = trainerId;
        this.entryState = entryState;

        this.w = (int)((this.getWidth() - this.totalInnerPadding())/INNER_SCALE);
        this.h = this.getHeight() / 6;
        this.y = 0;

        var displayName = entryState == EntryState.UNKNOWN ? "???" : trainer.getTrainerTeam().getName();
        var identity = entryState == EntryState.UNKNOWN ? "???" : trainer.getTrainerTeam().getIdentity();
        var backX = (int)(this.w*0.9);

        this.back = new HoverElement<>(
            new MultiStyleStringWidget(this, backX, this.y, this.w - backX, h, toComponent("[X]"), this.font).addStyle(Style.EMPTY.withColor(ChatFormatting.RED)).alignRight(),
            msw -> msw.setStyle(1), msw -> msw.setStyle(0));

        this.number = new MultiStyleStringWidget(this, 0, this.y, this.w, this.h, toComponent(String.format("%04d: ", trainerNr)), this.font).alignLeft();
        this.name = new MultiStyleStringWidget(this, (int)(this.w*0.18), this.y, (int)(this.w*0.72), this.h, entryState == EntryState.HIDDEN_KEY ? toComponent(displayName)
            .withStyle(ChatFormatting.OBFUSCATED) : toComponent(displayName), this.font)
            .scrolling().alignLeft();

        if(entryState != EntryState.DISCOVERED || identity.equals(displayName)) {
            this.aka = this.identity = null;
        } else {
            this.aka = new MultiStyleStringWidget(this, 0, this.y += this.h, this.w, this.h, toComponent("aka"), this.font).alignLeft();
            this.identity = new MultiStyleStringWidget(this, (int)(this.w*0.18), this.y, this.w, this.h, entryState == EntryState.HIDDEN_KEY ? toComponent(identity).withStyle(ChatFormatting.OBFUSCATED) : toComponent(identity), this.font).alignLeft();
        }

        this.back.element.active = true;
        this.contents.clear();
        this.contents.add(initOverviewPage());

        if(entryState != EntryState.UNKNOWN) {
            this.contents.add(initSpawningPage());
        }

        if(entryState == EntryState.DISCOVERED) {
            this.contents.add(initTeamPage());
            // this.contents.add(initLootPage()); // TODO: maybe
        }

        this.setPage(0);
        this.updateInnerHeight();
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
        this.y = this.identity == null ? 0 : this.h;

        pc.renderables.add(new StringWidget(8, this.y += this.h, this.w, this.h, toComponent("Type: "), this.font).alignLeft());
        pc.renderables.add(new StringWidget(8, this.y, (int)(this.w*0.9), this.h, toComponent(this.trainer.getType().name()), this.font).alignRight());

        pc.renderables.add(new StringWidget(8, this.y += this.h, this.w, this.h, toComponent("Level Caps: "), this.font).alignLeft());
        pc.renderables.add(new StringWidget(8, this.y, (int)(this.w*0.9), this.h, toComponent(String.format("%d -> %d", this.trainer.getRequiredLevelCap(), this.trainer.getRewardLevelCap())), this.font).alignRight());
        pc.renderables.add(new StringWidget(8, this.y += this.h , this.w, this.h, toComponent("Required Trainers: "), this.font).alignLeft());
        var tm = RCTMod.getInstance().getTrainerManager();

        this.trainer.getMissingRequirements(Set.of()).map(tm::getData).sorted((tmd1, tmd2) -> {
            var c = Integer.compare(tmd1.getRequiredLevelCap(), tmd2.getRequiredLevelCap());
            return c == 0 ? tmd1.getTrainerTeam().getName().compareTo(tmd2.getTrainerTeam().getName()) : c;
        }).forEach(tmd -> {
            pc.renderables.add(new StringWidget(16, this.y += this.h , this.w, this.h, toComponent(String.format("%s", tmd.getTrainerTeam().getName())), this.font).alignLeft());
        });

        pc.height = this.y + this.h;
        return pc;
    }

    private PageContent initSpawningPage() {
        var pc = initPage("Spawning");
        var mc = Minecraft.getInstance();
        var reg = mc.level.registryAccess().registryOrThrow(Registries.BIOME);
        var config = RCTMod.getInstance().getServerConfig();
        var biomes = new PriorityQueue<ResourceLocation>((r1, r2) -> {
            int i = r1.getNamespace().compareTo(r2.getNamespace());
            return i == 0 ? r1.getPath().compareTo(r2.getPath()) : i;
        });

        this.y = this.identity == null ? 0 : this.h;
        var spawnerItems = config.spawnerItemsFor(this.trainerId);
        
        if(spawnerItems.size() > 0) {
            pc.renderables.add(new StringWidget(8, this.y += this.h , this.w, this.h, toComponent("Items:"), this.font).alignLeft());
            spawnerItems.forEach(item -> pc.renderables.add(new StringWidget(16, this.y += this.h, this.w, this.h, toComponent(item.getDefaultInstance().getHoverName().getString()), this.font).alignLeft()));
        }

        pc.renderables.add(new StringWidget(8, this.y += this.h , this.w, this.h, toComponent("Biomes:"), this.font).alignLeft());

        reg.holders().forEach(holder -> {
            var tags = holder.tags()
                .map(t -> t.location().getNamespace() + ":" + t.location().getPath())
                .collect(Collectors.toSet());

            // given tags without namespace match with any namespace
            holder.tags()
                .map(t -> t.location().getPath())
                .forEach(t -> tags.add(t));

            if(config.biomeTagBlacklist().stream().noneMatch(tags::contains)
                && this.trainer.getBiomeTagBlacklist().stream().noneMatch(tags::contains)
                && (config.biomeTagWhitelist().isEmpty() || config.biomeTagWhitelist().stream().anyMatch(tags::contains))
                && (this.trainer.getBiomeTagWhitelist().isEmpty() || this.trainer.getBiomeTagWhitelist().stream().anyMatch(tags::contains)))
            {
                // see DebugScreenOverlay#printBiome
                biomes.offer((ResourceLocation)holder.unwrap().map(
                    r -> r.location(), b -> ResourceLocation.fromNamespaceAndPath("[unregistered]", b.toString())));
            }
        });

        var namespace = "";

        while(!biomes.isEmpty()) {
            var rs = biomes.poll();

            if(!namespace.equals(rs.getNamespace())) {
                pc.renderables.add(new StringWidget(16, this.y += this.h , this.w, this.h, toComponent(TextUtils.trim(rs.getNamespace(), MAX_BIOME_LENGTH)), this.font).alignLeft());
                namespace = rs.getNamespace();
            }

            pc.renderables.add(new StringWidget(20, this.y += this.h , this.w, this.h, toComponent(TextUtils.trim(rs.getPath(), MAX_BIOME_LENGTH)), this.font).alignLeft());
        }

        pc.height = this.y + this.h;
        return pc;
    }

    private PageContent initTeamPage() {
        var pc = initPage("Team");
        this.y = this.identity == null ? 0 : this.h;
        pc.height = this.y + this.h;

        this.trainer.getTrainerTeam().getTeam().forEach(poke -> {
            pc.renderables.add(new StringWidget(8, this.y += this.h , this.w, this.h, toComponent(TextUtils.trim(Locations.withoutNamespace(poke.getSpecies()), MAX_SPECIES_LENGTH)), this.font).alignLeft());
            pc.renderables.add(new StringWidget(8, this.y, (int)(this.w*0.9), this.h, toComponent(poke.getLevel()), this.font).alignRight());
        });

        return pc;
    }

    // private PageContent initLootPage() {
    //     var pc = initPage("Loot");
    //     this.y = this.identity == null ? 0 : this.h;
    //     pc.height = this.y + this.h;
    //     return pc;
    // }

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
        if(page != this.page) {
            this.page = Math.max(0, Math.min(this.contents.size() - 1, page));
            this.updateInnerHeight();
            this.setScrollAmount(0);
        }
    }

    public void setOnBackClicked(Consumer<Integer> backClickedHandler) {
        this.backClickedHandler = backClickedHandler;
    }

    @Override
    protected int getInnerHeight() {
        return (int)(this.innerHeight*INNER_SCALE);
    }

    private void updateInnerHeight() {
        this.innerHeight = Math.max(this.getHeight() - this.totalInnerPadding(), this.getPageContent(this.getPage()).height);
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
