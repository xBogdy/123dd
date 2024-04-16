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
package com.gitlab.srcmc.rctmod.api.data.pack;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gitlab.srcmc.rctmod.ModCommon;
import com.google.gson.reflect.TypeToken;

import net.minecraft.resources.ResourceLocation;

public class TrainerMobData implements IDataPackObject {
    public enum Type {
        NORMAL, LEADER, E4, CHAMP, TEAM_ROCKET
    }

    private Type type = Type.NORMAL;
    private int rewardLevelCap;
    // private int requiredBadges; // deprecated
    // private int requiredBeatenE4; // deprecated
    // private int requiredBeatenChamps; // deprecated
    private Map<Type, Integer> requiredDefeats = new HashMap<>();

    private int maxTrainerWins = 3;
    private int maxTrainerDefeats = 1;
    private int battleCooldownTicks = 2000;

    private float spawnChance = 1F; // [0, 1]
    private Set<String> biomeTagBlacklist = new HashSet<>();
    private Set<String> biomeTagWhitelist = new HashSet<>();
    
    private transient Map<String, String[]> dialog = new HashMap<>();
    private transient ResourceLocation textureResource;
    private transient ResourceLocation lootTableResource;
    private transient TrainerTeam team;

    public TrainerMobData() {
        this.textureResource = new ResourceLocation(ModCommon.MOD_ID, "textures/" + DataPackManager.PATH_DEFAULT + ".png");
        this.lootTableResource = new ResourceLocation(ModCommon.MOD_ID, DataPackManager.PATH_DEFAULT);
        this.team = new TrainerTeam();
    }

    public TrainerMobData(TrainerMobData origin) {
        this.type = origin.type;
        this.rewardLevelCap = origin.rewardLevelCap;
        // this.requiredBadges = origin.requiredBadges;
        // this.requiredBeatenE4 = origin.requiredBeatenE4;
        // this.requiredBeatenChamps = origin.requiredBeatenChamps;
        this.requiredDefeats = Map.copyOf(origin.requiredDefeats);
        this.maxTrainerWins = origin.maxTrainerWins;
        this.maxTrainerDefeats = origin.maxTrainerDefeats;
        this.battleCooldownTicks = origin.battleCooldownTicks;
        this.biomeTagBlacklist = Set.copyOf(origin.biomeTagBlacklist);
        this.biomeTagWhitelist = Set.copyOf(origin.biomeTagWhitelist);
        this.dialog = Map.copyOf(origin.dialog);
        this.textureResource = origin.textureResource;
        this.lootTableResource = origin.lootTableResource;
        this.team = origin.team; // no need for deep copy since immutable
    }

    public Type getType() {
        return this.type;
    }

    public int getRewardLevelCap() {
        return this.rewardLevelCap;
    }

    public int getRequiredLevelCap() {
        return this.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0);
    }

    // // deprecated
    // public int getRequiredBadges() {
    //     return this.requiredBadges;
    // }

    // // deprecated
    // public int getRequiredBeatenE4() {
    //     return this.requiredBeatenE4;
    // }

    // // deprecated
    // public int getRequiredBeatenChamps() {
    //     return this.requiredBeatenChamps;
    // }

    public int getRequiredDefeats(Type type) {
        return this.requiredDefeats.getOrDefault(type, 0);
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

    public float getSpawnChance() {
        return this.spawnChance;
    }

    public Set<String> getBiomeTagBlacklist() {
        return Collections.unmodifiableSet(this.biomeTagBlacklist);
    }

    public Set<String> getBiomeTagWhitelist() {
        return Collections.unmodifiableSet(this.biomeTagWhitelist);
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

    public TrainerTeam getTeam() {
        return this.team;
    }

    @Override
    public void onLoad(DataPackManager dpm, String trainerId, String context) {
        var lootTableResource = dpm.findResource(trainerId, "loot_tables");
        var textureResource = dpm.findResource(trainerId, "textures");

        if(textureResource.isPresent()) {
            this.textureResource = textureResource.get();
        }

        if(lootTableResource.isPresent()) {
            // the loot table is loaded by net.minecraft.world.level.storage.loot.LootDataManager
            // which resolves a 'shorthand' resource location automatically.
            this.lootTableResource = new ResourceLocation(ModCommon.MOD_ID, lootTableResource.get().getPath()
                .replace("loot_tables/", "")
                .replace(".json", ""));
        }

        dpm.loadResource(trainerId, "dialogs",
            dialog -> this.dialog = dialog,
            new TypeToken<Map<String, String[]>>() {});

        if(this.dialog == null) {
            this.dialog = new HashMap<>();
        }

        var team = dpm.loadTrainerTeam(trainerId);

        if(team.isPresent()) {
            this.team = team.get();
        }
    }
}
