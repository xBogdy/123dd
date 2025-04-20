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
package com.gitlab.srcmc.rctmod.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gitlab.srcmc.rctmod.api.config.IServerConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public class ServerConfig implements IServerConfig {
    // spawning
    private final ConfigValue<Double> globalSpawnChanceValue;
    private final ConfigValue<Double> globalSpawnChanceMinimumValue;
    private final ConfigValue<Integer> spawnIntervalTicksValue;
    private final ConfigValue<Integer> spawnIntervalTicksMaximumValue;
    private final ConfigValue<Integer> maxHorizontalDistanceToPlayersValue;
    private final ConfigValue<Integer> minHorizontalDistanceToPlayersValue;
    private final ConfigValue<Integer> maxVerticalDistanceToPlayersValue;
    private final ConfigValue<Integer> maxTrainersPerPlayerValue;
    private final ConfigValue<Integer> maxTrainersTotalValue;
    private final ConfigValue<Integer> maxLevelDiffValue;
    private final ConfigValue<Boolean> spawningRequiresTrainerCardValue;
    private final ConfigValue<Boolean> spawnTrainerAssociationValue;
    private final ConfigValue<List<? extends String>> dimensionBlacklistValue;
    private final ConfigValue<List<? extends String>> dimensionWhitelistValue;
    private final ConfigValue<List<? extends String>> biomeTagBlacklistValue;
    private final ConfigValue<List<? extends String>> biomeTagWhitelistValue;
    // private final ConfigValue<List<? extends String>> trainerSpawnerItemsValue;

    private double globalSpawnChanceCached;
    private double globalSpawnChanceMinimumCached;
    private int spawnIntervalTicksCached;
    private int spawnIntervalTicksMaximumCached;
    private int maxHorizontalDistanceToPlayersCached;
    private int minHorizontalDistanceToPlayersCached;
    private int maxVerticalDistanceToPlayersCached;
    private int maxTrainersPerPlayerCached;
    private int maxTrainersTotalCached;
    private int maxLevelDiffCached;
    private boolean spawningRequiresTrainerCardCached;
    private boolean spawnTrainerAssociationCached;
    private List<? extends String> dimensionBlacklistCached;
    private List<? extends String> dimensionWhitelistCached;
    private List<? extends String> biomeTagBlacklistCached;
    private List<? extends String> biomeTagWhitelistCached;
    // private Map<String, List<String>> trainerSpawnerItemsParsed;
    // private Map<String, Set<Item>> trainerIdToSpawnerItems;

    // players
    private final ConfigValue<Integer> initialLevelCapValue;
    private final ConfigValue<Integer> additiveLevelCapRequirementValue;
    private final ConfigValue<Boolean> considerEmptySeriesCompletedValue;
    private final ConfigValue<Boolean> allowOverLevelingValue;

    private int initialLevelCapCached;
    private int additiveLevelCapRequirementCached;
    private boolean considerEmptySeriesCompletedCached;
    private boolean allowOverLevelingCached;

    // debug
    private final ConfigValue<Boolean> logSpawningValue;
    private boolean logSpawningCached;

    private final ModConfigSpec spec;

    public ServerConfig() {
        var builder = new ModConfigSpec.Builder();
        builder.push("Spawning");

        this.globalSpawnChanceValue = builder
            .comment(SEPARATOR, "A global factor that determines if a spawn attempt for a trainer is made.")
            .defineInRange("globalSpawnChance", IServerConfig.super.globalSpawnChance(), 0, 1);

        this.globalSpawnChanceMinimumValue = builder
            .comment(SEPARATOR,
                "The chance for a trainer to spawn will shrink towards this value based of how many",
                "trainers are already spawned in for a player. For example if a player has 0 trainers",
                "spawned for them the chance will be as configured by globalSpawnChance if a player",
                "has barely filled up their spawn cap (maxTrainersPerPlayer), i.e. only one more free",
                "spot is left, the chance for the last trainer will be as configured by globalSpawnChanceMinimum.",
                "Set to any value equal to or above globalSpawnChance to disable (e.g. 1.0).")
            .defineInRange("globalSpawnChanceMinimum", IServerConfig.super.globalSpawnChanceMinimum(), 0, 1);

        this.spawnIntervalTicksValue = builder
            .comment(SEPARATOR, "The interval in ticks at which a spawn attempt is made per player.")
            .defineInRange("spawnIntervalTicks", IServerConfig.super.spawnIntervalTicks(), 1, Integer.MAX_VALUE - 1);

        this.spawnIntervalTicksMaximumValue = builder
            .comment(SEPARATOR,
                "The spawn interval ticks will grow towards this value based of how many trainers are already",
                "spawned in for a player. For example if a player has 0 trainers spawned for them the spawn",
                "interval ticks will be as configured by spawnIntervalTicks, if a player has barely filled up their",
                "spawn cap (maxTrainersPerPlayer), i.e. only one more free spot is left, the spawn interval for the",
                "last trainer will be as configured by spawnIntervalTicksMaximum. Set to any value equal to or below",
                "spawnIntervalTicks to disable (e.g. 0).")
            .defineInRange("spawnIntervalTicksMaximum", IServerConfig.super.spawnIntervalTicksMaximum(), 0, Integer.MAX_VALUE - 1);

        this.maxHorizontalDistanceToPlayersValue = builder
            .comment(SEPARATOR, "The max horizontal distance a trainer can spawn from players.")
            .defineInRange("maxHorizontalDistanceToPlayers", IServerConfig.super.maxHorizontalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.minHorizontalDistanceToPlayersValue = builder
            .comment(SEPARATOR, "The min horizontal distance a trainer can spawn from players.")
            .defineInRange("minHorizontalDistanceToPlayers", IServerConfig.super.minHorizontalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.maxVerticalDistanceToPlayersValue = builder
            .comment(SEPARATOR, "The max vertical distance a trainer can spawn from players.")
            .defineInRange("maxVerticalDistanceToPlayers", IServerConfig.super.maxVerticalDistanceToPlayers(), 1, Integer.MAX_VALUE - 1);

        this.maxTrainersPerPlayerValue = builder
            .comment(SEPARATOR, "Spawn cap of trainers per player.")
            .defineInRange("maxTrainersPerPlayer", IServerConfig.super.maxTrainersPerPlayer(), 0, Integer.MAX_VALUE - 1);

        this.maxTrainersTotalValue = builder
            .comment(SEPARATOR,
                "Total trainer spawn cap. This value may be increased for servers with higher expected",
                "player numbers (> 4), for example (|players| + 1)*maxTrainersPerPlayer.")
            .defineInRange("maxTrainersTotal", IServerConfig.super.maxTrainersTotal(), 0, Integer.MAX_VALUE - 1);

        this.maxLevelDiffValue = builder
            .comment(SEPARATOR,
                "The maximum level difference between the strongest pokemon in the team of a player and the strongest",
                "pokemon in the team of a trainer to spawn for that player. The spawn weight decreases with a higher",
                "level difference. Trainers with pokemon above the level cap of a player are excluded.")
            .defineInRange("maxLevelDiff", IServerConfig.super.maxLevelDiff(), 0, 100);

        this.spawningRequiresTrainerCardValue = builder
            .comment(SEPARATOR,
                "If enabled trainers will only spawn naturally around players that have a trainer card",
                "in their inventory (does not affect trainer spawners).")
            .define("spawningRequiresTrainerCard", IServerConfig.super.spawningRequiresTrainerCard());

        this.spawnTrainerAssociationValue = builder
            .comment(SEPARATOR,
                "If enabled a single trainer association npc may spawn naturally nearby players that carry a trainer",
                "card and have either not started a series or completed their current series. One may also spawn nearby",
                "any player in proximity to a village (at least 3 occupied beds and a village center). These can",
                "spawn everywhere but will respect the 'dimensionBlacklist' and 'dimensionWhitelist' settings.")
            .define("spawnTrainerAssociation", IServerConfig.super.spawnTrainerAssociation());

        // TODO: proper value validation
        this.dimensionBlacklistValue = builder
                .comment(SEPARATOR,
                    "A comma separated list of dimensions (e.g. [\"multiworld:spawn\", \"minecraft:the_end\"]).",
                    "In these dimensions trainers will never spawn.")
                .defineList("dimensionBlacklist", IServerConfig.super.dimensionBlacklist(), String::new, element -> true);

        this.dimensionWhitelistValue = builder
                .comment(SEPARATOR,
                    "A comma separated list of dimensions (e.g. [\"multiworld:spawn\" , \"minecraft:the_end\"]).",
                    "Trainers may only spawn in these dimensions (unless the list is empty).")
                .defineList("dimensionWhitelist", IServerConfig.super.dimensionWhitelist(), String::new, element -> true);

        this.biomeTagBlacklistValue = builder
            .comment(SEPARATOR,
                "A comma separated list of biome tags (e.g. [\"is_overworld\", \"is_forest\"]).",
                "A biome may not have any of the given tags attached to it, for a trainer to spawn in that biome.",
                "Trainers may also have additional tags defined by a data pack.")
            .defineList("biomeTagBlacklist", IServerConfig.super.biomeTagBlacklist(), String::new, element -> true);

        this.biomeTagWhitelistValue = builder
            .comment(SEPARATOR,
                "A comma separated list of biome tags (e.g. [\"is_overworld\", \"is_forest\"]).",
                "A biome must have atleast one of the given tags attached to it, for a trainer to spawn in that",
                "biome (unless the list is empty). Trainers may also have additional tags defined by a data pack.")
            .defineList("biomeTagWhitelist", IServerConfig.super.biomeTagWhitelist(), String::new, element -> true);
        
        // this.trainerSpawnerItemsValue = builder
        //     .comment(SEPARATOR,
        //         "A list of items that can be used to configure a trainer spawner to spawn specific",
        //         "trainers. Every entry must define an item followed by a space seperated list of",
        //         "trainer ids (of which one will be randomly chosen to spawn).")
        //     .defineList("trainerSpawnerItems", ServerConfig.trainerSpawnerItemList(IServerConfig.super.trainerSpawnerItems()), String::new, element -> true);

        builder.pop();
        builder.push("Players");

        this.initialLevelCapValue = builder
            .comment(SEPARATOR, "Initial level cap of players. Pokemon will not gain any experience if at or above the level cap.")
            .defineInRange("initialLevelCap", IServerConfig.super.initialLevelCap(), 1, 100);

        this.additiveLevelCapRequirementValue = builder
            .comment(SEPARATOR,
                "The required level cap for trainers is based of the strongest pokemon in their team.",
                "This value will be added to the derived level cap. Example: A trainer with a Pikachu at level 50",
                "has a level cap requirement of 50. If the additiveLevelCapRequirement is -10 the required level cap",
                "of that trainer becomes 40, if it is 10 the level cap requirement becomes 60.")
            .define("additiveLevelCapRequirement", IServerConfig.super.additiveLevelCapRequirement());

        this.considerEmptySeriesCompletedValue = builder
            .comment(SEPARATOR,
                "Can an empty series be considered completed or uncompletable? You tell me. If enabled an empty series",
                "will always be considered completed hence rewarding players immediately with a level cap of 100 otherwise",
                "the level cap of players will be as configured by initialLevelCap (and additiveLevelCapRequirement).",
                "Note that players will start with an empty series by default.")
            .define("considerEmptySeriesCompleted", IServerConfig.super.considerEmptySeriesCompleted());

        this.allowOverLevelingValue = builder
            .comment(SEPARATOR,
                "If enabled the level cap of a players will not prevent their pokemon from gaining experience and leveling up.",
                "Trainers will still refuse to battle players that carry pokemon above their level cap!")
            .define("allowOverLeveling", IServerConfig.super.allowOverLeveling());

        builder.pop();
        builder.push("Debug");

        this.logSpawningValue = builder
            .comment(SEPARATOR, "If enabled additional information are printed to the log whenever a trainer spawns or despawns.")
            .define("logSpawning", IServerConfig.super.logSpawning());

        this.spec = builder.build();
        // this.trainerSpawnerItemsParsed = new HashMap<>();
        // this.trainerIdToSpawnerItems = new HashMap<>();
    }

    @Override
    public void reload() {
        // this.trainerSpawnerItemsParsed = new HashMap<>();
        // this.trainerIdToSpawnerItems = new HashMap<>();

        // for(var entry : this.trainerSpawnerItemsValue.get()) {
        //     ServerConfig.parseTrainerSpawnerItem(trainerSpawnerItemsParsed, entry);
        // }

        // this.trainerSpawnerItemsParsed.forEach((itemKey, tids) -> {
        //     var itemRl = ResourceLocation.parse(itemKey);

        //     if(BuiltInRegistries.ITEM.containsKey(itemRl)) {
        //         var item = BuiltInRegistries.ITEM.get(itemRl);

        //         tids.forEach(tid -> this.trainerIdToSpawnerItems.compute(tid, (k, v) -> {
        //             if(v == null) {
        //                 v = new HashSet<>();
        //             }

        //             v.add(item);
        //             return v;
        //         }));
        //     }
        // });

        this.updateCache();
    }

    private void updateCache() {
        this.globalSpawnChanceCached = this.globalSpawnChanceValue.get();
        this.globalSpawnChanceMinimumCached = this.globalSpawnChanceMinimumValue.get();
        this.spawnIntervalTicksCached = this.spawnIntervalTicksValue.get();
        this.spawnIntervalTicksMaximumCached = this.spawnIntervalTicksMaximumValue.get();
        this.maxHorizontalDistanceToPlayersCached = this.maxHorizontalDistanceToPlayersValue.get();
        this.minHorizontalDistanceToPlayersCached = this.minHorizontalDistanceToPlayersValue.get();
        this.maxVerticalDistanceToPlayersCached = this.maxVerticalDistanceToPlayersValue.get();
        this.maxTrainersPerPlayerCached = this.maxTrainersPerPlayerValue.get();
        this.maxTrainersTotalCached = this.maxTrainersTotalValue.get();
        this.maxLevelDiffCached = this.maxLevelDiffValue.get();
        this.spawningRequiresTrainerCardCached = this.spawningRequiresTrainerCardValue.get();
        this.spawnTrainerAssociationCached = this.spawnTrainerAssociationValue.get();
        this.dimensionBlacklistCached = List.copyOf(this.dimensionBlacklistValue.get());
        this.dimensionWhitelistCached = List.copyOf(this.dimensionWhitelistValue.get());
        this.biomeTagBlacklistCached = List.copyOf(this.biomeTagBlacklistValue.get());
        this.biomeTagWhitelistCached = List.copyOf(this.biomeTagWhitelistValue.get());
        this.initialLevelCapCached = this.initialLevelCapValue.get();
        this.additiveLevelCapRequirementCached = this.additiveLevelCapRequirementValue.get();
        this.considerEmptySeriesCompletedCached = this.considerEmptySeriesCompletedValue.get();
        this.allowOverLevelingCached = this.allowOverLevelingValue.get();
        this.logSpawningCached = this.logSpawningValue.get();
    }

    @Override
    public ModConfigSpec getSpec() {
        return this.spec;
    }

    @Override
    public double globalSpawnChance() {
        return this.globalSpawnChanceCached;
    }

    @Override
    public double globalSpawnChanceMinimum() {
        return this.globalSpawnChanceMinimumCached;
    }

    @Override
    public int spawnIntervalTicks() {
        return this.spawnIntervalTicksCached;
    }

    @Override
    public int spawnIntervalTicksMaximum() {
        return this.spawnIntervalTicksMaximumCached;
    }

    @Override
    public int maxHorizontalDistanceToPlayers() {
        return this.maxHorizontalDistanceToPlayersCached;
    }

    @Override
    public int minHorizontalDistanceToPlayers() {
        return this.minHorizontalDistanceToPlayersCached;
    }

    @Override
    public int maxVerticalDistanceToPlayers() {
        return this.maxVerticalDistanceToPlayersCached;
    }

    @Override
    public int maxTrainersPerPlayer() {
        return this.maxTrainersPerPlayerCached;
    }

    @Override
    public int maxTrainersTotal() {
        return this.maxTrainersTotalCached;
    }

    @Override
    public int maxLevelDiff() {
        return this.maxLevelDiffCached;
    }

    @Override
    public boolean spawningRequiresTrainerCard() {
        return this.spawningRequiresTrainerCardCached;
    }

    @Override
    public boolean spawnTrainerAssociation() {
        return this.spawnTrainerAssociationCached;
    }

    @Override
    public List<? extends String> dimensionBlacklist() {
        return this.dimensionBlacklistCached;
    }

    @Override
    public List<? extends String> dimensionWhitelist() {
        return this.dimensionWhitelistCached;
    }

    @Override
    public List<? extends String> biomeTagBlacklist() {
        return this.biomeTagBlacklistCached;
    }

    @Override
    public List<? extends String> biomeTagWhitelist() {
        return this.biomeTagWhitelistCached;
    }

    // @Override
    // public Map<String, List<String>> trainerSpawnerItems() {
    //     return Collections.unmodifiableMap(this.trainerSpawnerItemsParsed);
    // }

    // @Override
    // public Set<Item> spawnerItemsFor(String trainerId) {
    //     return Collections.unmodifiableSet(this.trainerIdToSpawnerItems.getOrDefault(trainerId, Set.of()));
    // }

    @Override
    public int initialLevelCap() {
        return this.initialLevelCapCached;
    }

    @Override
    public int additiveLevelCapRequirement() {
        return this.additiveLevelCapRequirementCached;
    }

    @Override
    public boolean considerEmptySeriesCompleted() {
        return this.considerEmptySeriesCompletedCached;
    }

    @Override
    public boolean allowOverLeveling() {
        return this.allowOverLevelingCached;
    }

    @Override
    public boolean logSpawning() {
        return this.logSpawningCached;
    }

    // public static void parseTrainerSpawnerItem(Map<String, List<String>> target, String trainerSpawnerItem) {
    //     var values = trainerSpawnerItem.split(" ");

    //     if(values.length > 1) {
    //         // TODO: log errors (validation here?)
    //         target.put(values[0], Arrays.stream(values).skip(1).toList());
    //     }
    // }

    // public static List<String> trainerSpawnerItemList(Map<String, List<String>> trainerSpawnerItems) {
    //     var list = new ArrayList<String>();

    //     for(var entry : trainerSpawnerItems.entrySet()) {
    //         list.add(String.format("%s%s", entry.getKey(), entry.getValue().stream().reduce("", (a, b) -> a + ' ' + b)));
    //     }

    //     return list;
    // }
}
