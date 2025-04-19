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
package com.gitlab.srcmc.rctmod.api.data.save;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;
import com.gitlab.srcmc.rctmod.api.utils.ChatUtils;
import com.gitlab.srcmc.rctmod.api.utils.LangKeys;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class TrainerPlayerData extends SavedData {
    private Set<String> defeatedTrainerIds = new HashSet<>();
    private Map<String, Integer> completedSeries = new HashMap<>();
    private String currentSeries = "";
    private Player player;
    private int initialLevelCap, additiveLevelCapRequirement, levelCap;
    private boolean currentSeriesCompleted;

    public TrainerPlayerData(Player player) {
        this.player = player;
    }

    public TrainerPlayerData forPlayer(Player player) {
        this.player = player;
        return this;
    }

    public int getLevelCap() {
        var cfg = RCTMod.getInstance().getServerConfig();

        if(RCTMod.getInstance().getTrainerManager().updateRequired(this.player) || cfg.initialLevelCap() != this.initialLevelCap || cfg.additiveLevelCapRequirement() != this.additiveLevelCapRequirement) {
            this.additiveLevelCapRequirement = cfg.additiveLevelCapRequirement();
            this.initialLevelCap = cfg.initialLevelCap();
            this.updateLevelCap();
        }

        return this.levelCap;
    }

    private void updateLevelCap() {
        if(this.currentSeriesCompleted) {
            this.levelCap = 100; // TODO: maybe something like 'maxSeriesLevel'?
        } else {
            var cfg = RCTMod.getInstance().getServerConfig();
            this.levelCap = Math.max(RCTMod.getInstance().getTrainerManager().getMinRequiredLevelCap(this.getCurrentSeries()), Math.min(100, cfg.initialLevelCap() + cfg.additiveLevelCapRequirement()));
        }

        PlayerState.get(this.player).setLevelCap(this.levelCap);

        if(!this.currentSeriesCompleted) {
            this.defeatedTrainerIds.forEach(this::updateLevelCap);
        }
    }

    private void updateLevelCap(String trainerId) {
        if(this.currentSeriesCompleted) {
            this.levelCap = 100; // TODO: maybe something like 'maxSeriesLevel'?
        } else {
            var tmd = RCTMod.getInstance().getTrainerManager().getData(trainerId);
            this.levelCap = Math.max(this.levelCap, Math.max(1, Math.min(100, Math.max(tmd.getRequiredLevelCap(), tmd.getRewardLevelCap()))));
        }

        PlayerState.get(this.player).setLevelCap(this.levelCap);
    }

    public Set<String> getDefeatedTrainerIds() {
        return Collections.unmodifiableSet(this.defeatedTrainerIds);
    }

    public boolean addProgressDefeat(String trainerId) {
        var graph = RCTMod.getInstance().getSeriesManager().getGraph(this.currentSeries);
        var node = graph.get(trainerId);
        var updated = new boolean[]{false};

        if(node != null) {
            var ps = PlayerState.get(this.player);

            Stream.concat(Stream.of(node), node.siblings()).filter(tn -> !tn.isAlone()).forEach(tn -> {
                if(this.defeatedTrainerIds.add(tn.id())) {
                    this.updateLevelCap(tn.id());
                    ps.addProgressDefeat(tn.id());
                    this.updateCurrentSeries();
                    this.setDirty();
                    updated[0] = true;
                }
            });
        }

        return updated[0];
    }

    public boolean removeProgressDefeat(String trainerId) {
        var graph = RCTMod.getInstance().getSeriesManager().getGraph(this.currentSeries);
        var node = graph.get(trainerId);
        var updated = new boolean[]{false};

        if(node != null) {
            var ps = PlayerState.get(this.player);

            Stream.concat(Stream.of(node), node.siblings()).forEach(tn -> {
                if(this.defeatedTrainerIds.remove(tn.id())) {
                    this.updateLevelCap();
                    ps.removeProgressDefeat(tn.id());
                    this.setDirty();
                    updated[0] = true;
                }
            });
        }

        if(!this.isSeriesCompleted()) {
            this.currentSeriesCompleted = false; // must be 'dirty' or something else went wron earlier
        }

        return updated[0];
    }

    public boolean removeProgressDefeats() {
        return this.removeProgressDefeats(false);
    }

    protected boolean removeProgressDefeats(boolean forceUpdate) {
        if(forceUpdate || this.defeatedTrainerIds.size() > 0) {
            var ps = PlayerState.get(this.player);
            this.defeatedTrainerIds.clear();
            this.currentSeriesCompleted = false;
            this.updateLevelCap();
            ps.removeProgressDefeats();
            this.setDirty();
            return true;
        }

        return false;
    }

    public boolean isSeriesCompleted() {
        if(!this.currentSeries.isEmpty()) {
            return RCTMod.getInstance()
                .getSeriesManager().getGraph(this.currentSeries)
                .getRemaining(this.defeatedTrainerIds).size() == 0;
        }

        return false;
    }

    // will only ever return true once after a series has been set and was completed
    private boolean testSeriesCompleted() {
        if(!this.currentSeries.isEmpty() && !this.currentSeriesCompleted) {
            this.currentSeriesCompleted = RCTMod.getInstance()
                .getSeriesManager().getGraph(this.currentSeries)
                .getRemaining(this.defeatedTrainerIds).size() == 0;

            if(this.currentSeriesCompleted) {
                this.setDirty();
            }

            return this.currentSeriesCompleted;
        }

        return false;
    }

    private void updateCurrentSeries() {
        if(this.testSeriesCompleted()) {
            this.addSeriesCompletion(this.getCurrentSeries());
            this.updateLevelCap();
            ChatUtils.sendTitle(this.player, Component.translatable(LangKeys.GUI_TITLE_SERIES_COMPLETED).getString(), RCTMod.getInstance().getSeriesManager().getGraph(this.getCurrentSeries()).getMetaData().title().asComponent().getString());
            // ModRegistries.CriteriaTriggers.DEFEAT_COUNT.get().trigger((ServerPlayer)player, mob); // TODO: series completion advancements?
        }
    }

    public String getCurrentSeries() {
        return this.currentSeries;
    }

    public void setCurrentSeries(String seriesId) {
        this.setCurrentSeries(seriesId, false);
    }

    public void setCurrentSeries(String seriesId, boolean keepProgress) {
        if(!seriesId.equals(this.currentSeries)) {
            this.currentSeries = seriesId;
            PlayerState.get(this.player).setCurrentSeries(this.currentSeries);
            this.setDirty();
        }

        if(!keepProgress) {
            this.removeProgressDefeats(true);

            if(this.currentSeriesCompleted) {
                this.currentSeriesCompleted = false;
                this.setDirty();
            }
        }
    }

    public Map<String, Integer> getCompletedSeries() {
        return Collections.unmodifiableMap(this.completedSeries);
    }

    public void setSeriesCompletion(String seriesId, int n) {
        var m = this.completedSeries.getOrDefault(seriesId, 0);
        this.addSeriesCompletion(seriesId, n - m);
    }

    public void addSeriesCompletion(String seriesId) {
        this.addSeriesCompletion(seriesId, 1);
    }

    public void addSeriesCompletion(String seriesId, int n) {
        this.completedSeries.compute(seriesId, (k, v) -> v == null ? (n > 0 ? n : null) : n + v > 0 ? n + v : null);
    }

    public void removeSeriesCompletion(String seriesId) {
        this.removeSeriesCompletion(seriesId, this.completedSeries.getOrDefault(seriesId, 0));
    }

    public void removeSeriesCompletion(String seriesId, int n) {
        this.addSeriesCompletion(seriesId, -n);
    }

    public float getBonusLuck() {
        return this.getBonusLuck(0);
    }

    public float getBonusLuck(int extra) {
        var sm = RCTMod.getInstance().getSeriesManager();

        return (float)calculateLuck(extra + this.getCompletedSeries().entrySet().stream()
            .map(e -> sm.getGraph(e.getKey()).getMetaData().difficulty()*e.getValue())
            .reduce(0, (a, b) -> a + b));
    }

    public void sync() {
        var ps = PlayerState.get(this.player);
        ps.setLevelCap(this.levelCap);
        ps.setCurrentSeries(this.currentSeries);
        this.defeatedTrainerIds.forEach(ps::addProgressDefeat);
        RCTMod.getInstance().getTrainerManager().requiresUpdate(this.player);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, Provider provider) {
        byte b = 0;
        var progressDefeats = new CompoundTag();
        var completedSeries = new CompoundTag();
        this.defeatedTrainerIds.forEach(tid -> progressDefeats.putByte(tid, b));
        this.completedSeries.forEach((s, c) -> completedSeries.putInt(s, c));
        compoundTag.put("progressDefeats", progressDefeats);
        compoundTag.put("completedSeries", completedSeries);
        compoundTag.putString("currentSeries", this.currentSeries);
        compoundTag.putBoolean("currentSeriesCompleted", this.currentSeriesCompleted);
        return compoundTag;
    }

    // luck₀ := 0
    // luckₙ := luckₙ₋₁ + (1 - luckₙ₋₁) / (n * 8) with n > 0, converges to 1
    static final int LUCK_DELTA = 8;
    static double calculateLuck(int n) {
        var r = 0.0;
        
        for(int i = 1; i <= n; i++) {
            r += (1.0 - r) / (i * LUCK_DELTA);
        }
        
        return r;
    }

    public static class Builder {
        private Player player;

        public Builder(Player player) {
            this.player = player;
        }

        public TrainerPlayerData create() {
            var tpd = new TrainerPlayerData(this.player);
            tpd.updateLevelCap();
            return tpd;
        }

        public TrainerPlayerData of(CompoundTag tag, Provider provider) {
            var tpd = new TrainerPlayerData(this.player);
            var tm = RCTMod.getInstance().getTrainerManager();

            if(tag.contains("currentSeries")) {
                tpd.setCurrentSeries(tag.getString("currentSeries"));
            } else {
                // pre 0.14 -> assign to radicalred series
                tpd.setCurrentSeries("radicalred");
            }

            if(tag.contains("completedSeries")) {
                var seriesTag = tag.getCompound("completedSeries");
                seriesTag.getAllKeys().forEach(s -> tpd.setSeriesCompletion(s, seriesTag.getInt(s)));
            }

            if(tag.contains("currentSeriesCompleted")) {
                tpd.currentSeriesCompleted = tag.getBoolean("currentSeriesCompleted");
            }

            if(!tpd.currentSeries.isEmpty()) {
                if(tag.contains("progressDefeats")) {
                    tag.getCompound("progressDefeats").getAllKeys().forEach(tpd::addProgressDefeat);
                } else {
                    // legacy support (< 0.14): derive progress defeats from trainer defeat counts
                    var level = this.player.getServer().overworld();
                    
                    tm.getAllData()
                        .map(entry -> entry.getKey())
                        .filter(tid -> tm.getBattleMemory(level, tid).getDefeatByCount(tid, this.player) > 0)
                        .forEach(tpd::addProgressDefeat);
                }
            }

            tpd.updateLevelCap();
            return tpd;
        }
    }

    public static String filePath(Player player) {
        return String.format("%s.player.%s.stat", ModCommon.MOD_ID, player.getUUID().toString());
    }
}
