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
package com.gitlab.srcmc.rctmod.api.data.pack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;

public class TrainerMobData implements IDataPackObject {
    private String type = "normal";
    private List<Set<String>> requiredDefeats = new ArrayList<>();
    private Set<String> series = new HashSet<>();
    private Set<String> substitutes = new HashSet<>();
    private boolean optional;
    
    private int maxTrainerWins = 3;
    private int maxTrainerDefeats = 1;
    private int battleCooldownTicks = 240;
    
    private float spawnWeightFactor = 1F; // >= 0
    private Set<String> biomeTagBlacklist = new HashSet<>();
    private Set<String> biomeTagWhitelist = new HashSet<>();
    
    private transient int rewardLevelCap;
    private transient Set<String> followdBy = new HashSet<>();
    private transient Map<String, String[]> dialog = new HashMap<>();
    private transient ResourceLocation textureResource;
    private transient ResourceLocation lootTableResource;
    private transient TrainerTeam trainerTeam;
    private transient Supplier<TrainerType> lazyType = () -> {
        var type = TrainerType.valueOf(this.type.toLowerCase());
        this.lazyType = () -> type;
        return this.lazyType.get();
    };

    public TrainerMobData() {
        this.textureResource = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/" + DataPackManager.PATH_DEFAULT + ".png");
        this.lootTableResource = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, DataPackManager.PATH_DEFAULT);
        this.trainerTeam = new TrainerTeam();
    }

    public TrainerMobData(TrainerMobData origin) {
        this.type = origin.type;
        this.rewardLevelCap = origin.rewardLevelCap;
        this.requiredDefeats = List.copyOf(origin.requiredDefeats);
        this.followdBy = Set.copyOf(origin.followdBy);
        this.optional = origin.optional;
        this.maxTrainerWins = origin.maxTrainerWins;
        this.maxTrainerDefeats = origin.maxTrainerDefeats;
        this.battleCooldownTicks = origin.battleCooldownTicks;
        this.biomeTagBlacklist = Set.copyOf(origin.biomeTagBlacklist);
        this.biomeTagWhitelist = Set.copyOf(origin.biomeTagWhitelist);
        this.dialog = Map.copyOf(origin.dialog);
        this.textureResource = origin.textureResource;
        this.lootTableResource = origin.lootTableResource;
        this.trainerTeam = origin.trainerTeam; // no need for deep copy since immutable
    }

    public TrainerType getType() {
        return this.lazyType.get();
    }

    public void setRewardLevelCap(int levelCap) {
        var cfg = RCTMod.getInstance().getServerConfig();
        this.rewardLevelCap = Math.min(100, Math.max(1, Math.max(cfg.initialLevelCap() + cfg.additiveLevelCapRequirement(), levelCap)));
    }

    public int getRewardLevelCap() {
        return this.rewardLevelCap;
    }

    public int getRequiredLevelCap() {
        var cfg = RCTMod.getInstance().getServerConfig();
        return Math.max(1, Math.min(100, this.getTrainerTeam().getTeam().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0) + cfg.additiveLevelCapRequirement()));
    }

    public boolean isOfSeries(String seriesId) {
        return this.series.isEmpty() || this.series.contains(seriesId);
    }

    public Stream<String> getSeries() {
        return this.series.stream();
    }

    public List<Set<String>> getRequiredDefeats() {
        return Collections.unmodifiableList(this.requiredDefeats);
    }

    public Set<String> getSubstitutes() {
        return Collections.unmodifiableSet(this.substitutes);
    }

    public Stream<String> getMissingRequirements(Set<String> defeatedTrainerIds) {
        return this.getMissingRequirements(defeatedTrainerIds, false);
    }

    public Stream<String> getMissingRequirements(Set<String> defeatedTrainerIds, boolean includeAlternatives) {
        return this.requiredDefeats.stream().mapMulti((set, cons) -> {
            if(includeAlternatives) {
                if(set.stream().noneMatch(defeatedTrainerIds::contains)) {
                    set.stream().forEach(cons);
                }
            } else if(!set.isEmpty() && set.stream().noneMatch(defeatedTrainerIds::contains)) {
                cons.accept(set.stream().findFirst().get());
            }
        });
    }

    public boolean isOptional() {
        return this.optional;
    }

    public int getMaxTrainerWins() {
        return this.maxTrainerWins;
    }

    public int getMaxTrainerDefeats() {
        return this.maxTrainerDefeats;
    }

    public int getBattleCooldownTicks() {
        return this.battleCooldownTicks;
    }

    public float getSpawnWeightFactor() {
        return this.spawnWeightFactor;
    }

    public Set<String> getBiomeTagBlacklist() {
        return Collections.unmodifiableSet(this.biomeTagBlacklist);
    }

    public Set<String> getBiomeTagWhitelist() {
        return Collections.unmodifiableSet(this.biomeTagWhitelist);
    }

    public Set<String> getFollowedBy() {
        return Collections.unmodifiableSet(this.followdBy);
    }
    
    public boolean addFollowedBy(String trainerId) {
        return this.followdBy.add(trainerId);
    }

    public boolean removeFollowdBy(String trainerId) {
        return this.followdBy.remove(trainerId);
    }

    public void clearFollowedBy(String trainerId) {
        this.followdBy.clear();
    }

    public Map<String, String[]> getDialog() {
        return Collections.unmodifiableMap(this.dialog);
    }

    public ResourceLocation getTextureResource() {
        return this.textureResource;
    }

    public ResourceLocation getLootTableResource() {
        return this.lootTableResource;
    }

    public TrainerTeam getTrainerTeam() {
        return this.trainerTeam;
    }

    @Override
    public void onLoad(DataPackManager dpm, String trainerId, String context) {
        var lootTableResource = dpm.findResource(trainerId, "loot_table");
        var textureResource = dpm.findResource(trainerId, "textures");

        if(textureResource.isPresent()) {
            this.textureResource = textureResource.get();
        }

        if(lootTableResource.isPresent()) {
            // the loot table is loaded by net.minecraft.world.level.storage.loot.LootDataManager
            // which resolves a 'shorthand' resource location automatically.
            this.lootTableResource = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, lootTableResource.get().getPath()
                .replace("loot_table/", "")
                .replace(".json", ""));
        }

        dpm.loadResource(trainerId, "dialogs",
            dialog -> this.dialog = dialog,
            new TypeToken<Map<String, String[]>>() {});

        if(this.dialog == null) {
            this.dialog = new HashMap<>();
        }

        var trainerTeam = dpm.loadTrainerTeam(trainerId);

        if(trainerTeam.isPresent()) {
            this.trainerTeam = trainerTeam.get();
        }
    }
}
