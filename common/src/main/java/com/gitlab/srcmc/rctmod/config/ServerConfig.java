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

import com.gitlab.srcmc.rctmod.api.config.IServerConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.config.ModConfig;

public class ServerConfig extends ForgeConfig implements IServerConfig {
    // spawning
    private ConfigValue<Double> globalSpawnChanceValue; // TODO: has currently no effect
    private ConfigValue<Integer> despawnDelayTicksValue;
    private ConfigValue<Integer> spawnIntervalTicksValue;
    private ConfigValue<Integer> maxHorizontalDistanceToPlayersValue;
    private ConfigValue<Integer> minHorizontalDistanceToPlayersValue;
    private ConfigValue<Integer> maxVerticalDistanceToPlayersValue;
    private ConfigValue<Integer> maxTrainersPerPlayerValue;
    private ConfigValue<Integer> maxTrainersTotalValue;
    private ConfigValue<Integer> maxLevelDiffValue;

    // player
    private ConfigValue<Integer> initialLevelCapValue; // TODO: has currently no effect

    private ForgeConfigSpec spec;

    public ServerConfig() {
        super(ModConfig.Type.SERVER);

        var builder = createBuilder();
        builder.push("Spawning");

        this.globalSpawnChanceValue = builder
            .comment("A global factor that determines if a spawn attempt for a trainer is made.")
            .defineInRange("globalSpawnChanceValue", IServerConfig.super.globalSpawnChance(), 0, 1);

        this.despawnDelayTicksValue = builder
            .comment("Number of ticks after which a trainer will despawn if far away from players. Trainers that cannot battle anymore will despawn immediately if far away.")
            .defineInRange("despawnDelayTicksValue", IServerConfig.super.despawnDelayTicks(), 1, Integer.MAX_VALUE - 1);

        this.spawnIntervalTicksValue = builder
            .comment("The interval in ticks at which a spawn attempt is made per player.")
            .defineInRange("spawnIntervalTicksValue", IServerConfig.super.spawnIntervalTicks(), 1, Integer.MAX_VALUE - 1);

        this.maxHorizontalDistanceToPlayersValue = builder
            .comment("The max horizontal distance a trainer can spawn from players.")
            .defineInRange("maxHorizontalDistanceToPlayersValue", IServerConfig.super.maxHorizontalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.minHorizontalDistanceToPlayersValue = builder
            .comment("The min horizontal distance a trainer can spawn from players.")
            .defineInRange("minHorizontalDistanceToPlayersValue", IServerConfig.super.minHorizontalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.maxVerticalDistanceToPlayersValue = builder
            .comment("The max vertical distance a trainer can spawn from players.")
            .defineInRange("maxVerticalDistanceToPlayersValue", IServerConfig.super.maxVerticalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.maxTrainersPerPlayerValue = builder
            .comment("Spawn cap of trainers per player.")
            .defineInRange("maxTrainersPerPlayerValue", IServerConfig.super.maxTrainersPerPlayer(), 0, Integer.MAX_VALUE - 1);

        this.maxTrainersTotalValue = builder
            .comment("Total trainer spawn cap.")
            .defineInRange("maxTrainersTotalValue", IServerConfig.super.maxTrainersTotal(), 0, Integer.MAX_VALUE - 1);

        this.maxLevelDiffValue = builder
            .comment("Total trainer spawn cap.")
            .defineInRange("maxTrainersTotalValue", IServerConfig.super.maxLevelDiff(), 0, 100);
        
        builder.pop();
        builder.push("Players");

        this.initialLevelCapValue = builder
            .comment("Initial level cap of players. Pokemon will not gain any experience if at or above the level.")
            .defineInRange("initialLevelCap", IServerConfig.super.initialLevelCap(), 1, 100);

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
    public int despawnDelayTicks() {
        return this.despawnDelayTicksValue.get();
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
    public int initialLevelCap() {
        return this.initialLevelCapValue.get();
    }
}
