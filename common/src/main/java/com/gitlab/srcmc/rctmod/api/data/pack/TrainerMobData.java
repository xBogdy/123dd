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
import com.gitlab.srcmc.rctmod.api.RCTMod;
import com.google.gson.reflect.TypeToken;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

public class TrainerMobData implements IDataPackObject {
    public enum Type {
        NORMAL, LEADER, BOSS, E4, CHAMP, TEAM_ROCKET, RIVAL;

        private static final Map<Type, String> symbols = Map.of(
            LEADER, "[L]",
            BOSS, "[B]",
            E4, "[E]",
            CHAMP, "[C]",
            TEAM_ROCKET, "[T]",
            RIVAL, "[R]"
        );

        private static final Map<Type, Integer> colors = Map.of(
            LEADER, ChatFormatting.GREEN.getColor(),
            BOSS, ChatFormatting.GOLD.getColor(),
            E4, ChatFormatting.BLUE.getColor(),
            CHAMP, ChatFormatting.LIGHT_PURPLE.getColor(),
            TEAM_ROCKET, ChatFormatting.DARK_GRAY.getColor()
        );

        public String toString() {
            return symbols.getOrDefault(this, "");
        }

        public int toColor() {
            return colors.getOrDefault(this, ChatFormatting.WHITE.getColor());
        }
    }

    private Type type = Type.NORMAL;
    private int rewardLevelCap;
    private Map<Type, Integer> requiredDefeats = new HashMap<>();

    private int maxTrainerWins = 3;
    private int maxTrainerDefeats = 1;
    private int battleCooldownTicks = 2000;

    private float spawnWeightFactor = 1F; // >= 0
    private Set<String> biomeTagBlacklist = new HashSet<>();
    private Set<String> biomeTagWhitelist = new HashSet<>();
    
    private transient Map<String, String[]> dialog = new HashMap<>();
    private transient ResourceLocation textureResource;
    private transient ResourceLocation lootTableResource;
    private transient TrainerTeam team;

    public TrainerMobData() {
        this.textureResource = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/" + DataPackManager.PATH_DEFAULT + ".png");
        this.lootTableResource = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, DataPackManager.PATH_DEFAULT);
        this.team = new TrainerTeam();
    }

    public TrainerMobData(TrainerMobData origin) {
        this.type = origin.type;
        this.rewardLevelCap = origin.rewardLevelCap;
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
        return this.rewardLevelCap < 100 ? Math.max(0, Math.min(100, this.rewardLevelCap + RCTMod.getInstance().getServerConfig().bonusLevelCap())) : 100;
    }

    public int getRequiredLevelCap() {
        var bonus = RCTMod.getInstance().getServerConfig().bonusLevelCap();
        return Math.max(0, Math.min(100, this.getTeam().getMembers().stream().map(p -> p.getLevel()).max(Integer::compare).orElse(0) + bonus));
    }

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

    public float getSpawnWeightFactor() {
        return this.spawnWeightFactor;
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
            this.lootTableResource = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, lootTableResource.get().getPath()
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
