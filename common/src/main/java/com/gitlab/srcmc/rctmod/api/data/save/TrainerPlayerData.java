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

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.gitlab.srcmc.rctmod.api.data.sync.PlayerState;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

public class TrainerPlayerData extends SavedData {
    private Set<String> defeatedTrainerIds = new HashSet<>();
    private Map<String, Integer> completedSeries = new HashMap<>();
    private String currentSeries = "";
    private Player player;
    private int initialLevelCap, additiveLevelCapRequirement, levelCap;

    public TrainerPlayerData(Player player) {
        this.player = player;
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
        var cfg = RCTMod.getInstance().getServerConfig();
        this.levelCap = Math.max(RCTMod.getInstance().getTrainerManager().getMinRequiredLevelCap(this.getCurrentSeries()), Math.min(100, cfg.initialLevelCap() + cfg.additiveLevelCapRequirement()));
        this.defeatedTrainerIds.forEach(this::updateLevelCap);
    }

    private void updateLevelCap(String trainerId) {
        var tmd = RCTMod.getInstance().getTrainerManager().getData(trainerId);
        this.levelCap = Math.max(this.levelCap, Math.max(1, Math.min(100, Math.max(tmd.getRequiredLevelCap(), tmd.getRewardLevelCap()))));
    }

    public Set<String> getDefeatedTrainerIds() {
        return Collections.unmodifiableSet(this.defeatedTrainerIds);
    }

    public boolean addProgressDefeat(String trainerId) {
        if(PlayerState.get(this.player).isKeyTrainer(trainerId) && this.defeatedTrainerIds.add(trainerId)) {
            var ps = PlayerState.get(this.player);
            this.updateLevelCap(trainerId);
            ps.addProgressDefeat(trainerId);
            this.setDirty();
            return true;
        }

        return false;
    }

    public boolean removeProgressDefeat(String trainerId) {
        if(this.defeatedTrainerIds.remove(trainerId)) {
            var ps = PlayerState.get(this.player);
            this.updateLevelCap();
            ps.removeProgressDefeat(trainerId);
            this.setDirty();
            return true;
        }

        return false;
    }

    public boolean removeProgressDefeats() {
        return this.removeProgressDefeats(false);
    }

    protected boolean removeProgressDefeats(boolean forceUpdate) {
        if(forceUpdate || this.defeatedTrainerIds.size() > 0) {
            var ps = PlayerState.get(this.player);
            this.defeatedTrainerIds.clear();
            this.updateLevelCap();
            ps.removeProgressDefeats();
            this.setDirty();
            return true;
        }

        return false;
    }

    public boolean isSeriesCompleted() {
        if(!this.currentSeries.isEmpty()) {
            return RCTMod.getInstance().getSeriesManager()
                .getRequiredDefeats(this.currentSeries, this.defeatedTrainerIds)
                .findFirst().isEmpty();
        }

        return false;
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
            this.setDirty();
        }

        if(!keepProgress) {
            this.removeProgressDefeats(true);
        }
    }

    public Map<String, Integer> getCompletedSeries() {
        return Collections.unmodifiableMap(this.completedSeries);
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
        return compoundTag;
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

            if(tag.contains("progressDefeats")) {
                var tm = RCTMod.getInstance().getTrainerManager();
                tpd.defeatedTrainerIds.addAll(tag.getCompound("progressDefeats")
                    .getAllKeys().stream()
                    .filter(tid -> !tm.getData(tid).getFollowdBy().isEmpty()/* || entry.getValue().getMissingRequirements(Set.of()).findFirst().isPresent()*/).toList());
            } else {
                // legacy support: derive progress defeats from trainer defeat counts
                var tm = RCTMod.getInstance().getTrainerManager();
                var level = this.player.getServer().overworld();

                tm.getAllData()
                    .filter(entry -> !entry.getValue().getFollowdBy().isEmpty()/* || entry.getValue().getMissingRequirements(Set.of()).findFirst().isPresent()*/)
                    .map(entry -> entry.getKey())
                    .filter(tid -> tm.getBattleMemory(level, tid).getDefeatByCount(this.player) > 0)
                    .forEach(tpd.defeatedTrainerIds::add);
            }

            if(tag.contains("completedSeries")) {
                var seriesTag = tag.getCompound("completedSeries");
                seriesTag.getAllKeys().forEach(s -> tpd.completedSeries.put(s, tag.getInt(s)));
            }

            if(tag.contains("currentSeries")) {
                tpd.currentSeries = tag.getString("currentSeries");
            }

            tpd.updateLevelCap();
            return tpd;
        }
    }

    public static String filePath(Player player) {
        return String.format("%s.player.%s.stat", ModCommon.MOD_ID, player.getUUID().toString());
    }
}
