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
package com.gitlab.srcmc.rctmod.config;

import java.util.List;

import com.gitlab.srcmc.rctmod.api.config.IServerConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig extends ForgeConfig implements IServerConfig {
    // spawning
    private ConfigValue<Double> globalSpawnChanceValue;
    private ConfigValue<Integer> spawnIntervalTicksValue;
    private ConfigValue<Integer> maxHorizontalDistanceToPlayersValue;
    private ConfigValue<Integer> minHorizontalDistanceToPlayersValue;
    private ConfigValue<Integer> maxVerticalDistanceToPlayersValue;
    private ConfigValue<Integer> maxTrainersPerPlayerValue;
    private ConfigValue<Integer> maxTrainersTotalValue;
    private ConfigValue<Integer> maxLevelDiffValue;
    private ConfigValue<List<? extends String>> biomeTagBlacklistValue;
    private ConfigValue<List<? extends String>> biomeTagWhitelistValue;

    // players
    private ConfigValue<Integer> initialLevelCapValue;
    private ConfigValue<Integer> maxOverLevelCapValue;
    private ConfigValue<Integer> bonusLevelCapValue;

    // debug
    private ConfigValue<Boolean> logSpawningValue;

    private ForgeConfigSpec spec;

    public ServerConfig() {
        super(ModConfig.Type.SERVER);

        var builder = createBuilder();
        builder.push("Spawning");

        this.globalSpawnChanceValue = builder
            .comment("A global factor that determines if a spawn attempt for a trainer is made.")
            .defineInRange("globalSpawnChance", IServerConfig.super.globalSpawnChance(), 0, 1);

        this.spawnIntervalTicksValue = builder
            .comment("The interval in ticks at which a spawn attempt is made per player.")
            .defineInRange("spawnIntervalTicks", IServerConfig.super.spawnIntervalTicks(), 1, Integer.MAX_VALUE - 1);

        this.maxHorizontalDistanceToPlayersValue = builder
            .comment("The max horizontal distance a trainer can spawn from players.")
            .defineInRange("maxHorizontalDistanceToPlayers", IServerConfig.super.maxHorizontalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.minHorizontalDistanceToPlayersValue = builder
            .comment("The min horizontal distance a trainer can spawn from players.")
            .defineInRange("minHorizontalDistanceToPlayers", IServerConfig.super.minHorizontalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.maxVerticalDistanceToPlayersValue = builder
            .comment("The max vertical distance a trainer can spawn from players.")
            .defineInRange("maxVerticalDistanceToPlayers", IServerConfig.super.maxVerticalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.maxTrainersPerPlayerValue = builder
            .comment("Spawn cap of trainers per player.")
            .defineInRange("maxTrainersPerPlayer", IServerConfig.super.maxTrainersPerPlayer(), 0, Integer.MAX_VALUE - 1);

        this.maxTrainersTotalValue = builder
            .comment("Total trainer spawn cap.")
            .defineInRange("maxTrainersTotal", IServerConfig.super.maxTrainersTotal(), 0, Integer.MAX_VALUE - 1);

        this.maxLevelDiffValue = builder
            .comment("The maximum level difference between the strongest pokemon in the team of a player and the strongest pokemon in the team of a trainer to spawn for that player. The spawn weight decreases with a higher level difference. Trainers with pokemon above the level cap of a player are excluded.")
            .defineInRange("maxLevelDiff", IServerConfig.super.maxLevelDiff(), 0, 100);

        this.biomeTagBlacklistValue = builder
            .comment("A comma separated list of biome tags (e.g. [\"is_overworld\", \"is_forest\"]). A biome may not have any of the given tags attached to it, for a trainer to spawn in that biome. Trainers may also have additional tags defined by a data pack.")
            .defineList("biomeTagBlacklist", IServerConfig.super.biomeTagBlacklist(), element -> true);

        this.biomeTagWhitelistValue = builder
            .comment("A comma separated list of biome tags (e.g. [\"is_overworld\", \"is_forest\"]). A biome must have atleast one of the given tags attached to it, for a trainer to spawn in that biome (unless the list is empty). Trainers may also have additional tags defined by a data pack.")
            .defineList("biomeTagWhitelist", IServerConfig.super.biomeTagWhitelist(), element -> true);

        builder.pop();
        builder.push("Players");

        this.initialLevelCapValue = builder
            .comment("Initial level cap of players. Pokemon will not gain any experience if at or above the level cap.")
            .defineInRange("initialLevelCap", IServerConfig.super.initialLevelCap(), 1, 100);

        this.maxOverLevelCapValue = builder
            .comment("Trainers will refuse to battle players that have pokemon in their party with a level greater than the set value + the level cap of the player. This value can also be negative.")
            .define("maxOverLevelCap", IServerConfig.super.maxOverLevelCap());

        this.bonusLevelCapValue = builder
            .comment("This is your one stop difficulty setting. The 'bonusLevelCap' is added to the 'initialLevelCap' aswell as any increased level cap rewarded by trainers (except of trainers that reward a level cap of 100). In short, a positive value will make this mod easier a negative value harder. On a side note, trainers will also take this value into account when determining the required level cap to fight them. For example if we assume bonusLevelCap=-3: A trainer with a strongest pokemon at level 15 would usually require a level cap of 15, now a level cap of 15-3=12 is required.")
            .define("bonusLevelCap", IServerConfig.super.bonusLevelCap());

        builder.pop();
        builder.push("Debug");

        this.logSpawningValue = builder
            .comment("If enabled additional information are printed to the log whenever a trainer spawns or despawns.")
            .define("logSpawning", IServerConfig.super.logSpawning());

        this.spec = builder.build();
    }

    @Override
    public ForgeConfigSpec getSpec() {
        return this.spec;
    }

    @Override
    public double globalSpawnChance() {
        return this.globalSpawnChanceValue.get();
    }

    @Override
    public int spawnIntervalTicks() {
        return this.spawnIntervalTicksValue.get();
    }

    @Override
    public int maxHorizontalDistanceToPlayers() {
        return this.maxHorizontalDistanceToPlayersValue.get();
    }

    @Override
    public int minHorizontalDistanceToPlayers() {
        return this.minHorizontalDistanceToPlayersValue.get();
    }

    @Override
    public int maxVerticalDistanceToPlayers() {
        return this.maxVerticalDistanceToPlayersValue.get();
    }

    @Override
    public int maxTrainersPerPlayer() {
        return this.maxTrainersPerPlayerValue.get();
    }

    @Override
    public int maxTrainersTotal() {
        return this.maxTrainersTotalValue.get();
    }

    @Override
    public int maxLevelDiff() {
        return this.maxLevelDiffValue.get();
    }

    @Override
    public List<? extends String> biomeTagBlacklist() {
        return this.biomeTagBlacklistValue.get();
    }

    @Override
    public List<? extends String> biomeTagWhitelist() {
        return this.biomeTagWhitelistValue.get();
    }

    @Override
    public int initialLevelCap() {
        return this.initialLevelCapValue.get();
    }

    @Override
    public int maxOverLevelCap() {
        return this.maxOverLevelCapValue.get();
    }

    @Override
    public int bonusLevelCap() {
        return this.bonusLevelCapValue.get();
    }

    @Override
    public boolean logSpawning() {
        return this.logSpawningValue.get();
    }
}
