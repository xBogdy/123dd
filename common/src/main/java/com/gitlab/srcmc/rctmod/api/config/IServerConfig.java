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
package com.gitlab.srcmc.rctmod.api.config;

import java.util.List;

public interface IServerConfig extends IModConfig {
    /**
     * Reload is invoked after the config was reloaded and all config values have been parsed.
     */
    default void reload() {}

    /**
     * A global factor that determines if a spawn attempt for a trainer is made.
     * 
     * range [0, 1]
     * default 0.85
     */
    default double globalSpawnChance() { return 0.85; }

    /**
     * The chance for a trainer to spawn will shrink towards this value based of how
     * many trainers are already spawned in for a player. For example if a player has 0
     * trainers spawned for them the chance will be as configured by globalSpawnChance,
     * if a player has barely filled up their spawn cap (maxTrainersPerPlayer), i.e.
     * only one more free spot is left, the chance for the last trainer will be as
     * configured by globalSpawnChanceMinimum. Set to any value equal to or above
     * globalSpawnChance to disable (e.g. 1.0).
     * 
     * range [0, 1]
     * default: 0.15
     */
    default double globalSpawnChanceMinimum() { return 0.15; }

    /**
     * The interval in ticks at which a spawn attempt is made per player.
     * 
     * range [1, inf]
     * default 180
     */
    default int spawnIntervalTicks() { return 180; }

    /**
     * The spawn interval ticks will grow towards this value based of how many trainers
     * are already spawned in for a player. For example if a player has 0 trainers
     * spawned for them the spawn interval ticks will be as configured by
     * spawnIntervalTicks, if a player has barely filled up their spawn cap
     * (maxTrainersPerPlayer), i.e. only one more free spot is left, the spawn interval
     * for the last trainer will be as configured by spawnIntervalTicksMaximum. Set to
     * any value equal to or below spawnIntervalTicks to disable (e.g. 0).
     * 
     * range [0, inf]
     * default: 1800
     */
    default int spawnIntervalTicksMaximum() { return 1800; }

    /**
     * If enabled trainers will only spawn naturally around players that have a trainer
     * card in their inventory (does not affect trainer spawners).
     * 
     * default: false
     */
    default boolean spawningRequiresTrainerCard() { return false; }

    /**
     * If enabled a single trainer association npc may spawn naturally nearby players
     * that carry a trainer card and have either not started a series or completed
     * their current series. One may also spawn nearby any player in proximity to a
     * village (at least 3 occupied beds and a village center). These can spawn
     * everywhere but will respect the 'dimensionBlacklist' and 'dimensionWhitelist'
     * settings.
     * 
     * default: true
     */
    default boolean spawnTrainerAssociation() { return true; }

    /**
     * The min horizontal distance a trainer can spawn from players.
     * 
     * range [1, inf]
     * default 25
     */
    default int minHorizontalDistanceToPlayers() { return 25; }

    /**
     * The max horizontal distance a trainer can spawn from players.
     * 
     * range [1, inf]
     * default 70
     */
    default int maxHorizontalDistanceToPlayers() { return 70; }

    /**
     * The max vertical distance a trainer can spawn from players.
     * 
     * range [1, inf]
     * default 30
     */
    default int maxVerticalDistanceToPlayers() { return 30; }

    /**
     * Spawn cap of trainers per player.
     * 
     * range [0, inf]
     * default 12
     */
    default int maxTrainersPerPlayer() { return 12; }

    /**
     * Total trainer spawn cap. This value may be increased for servers with higher
     * expected player numbers (> 4), for example (|players| + 1)*maxTrainersPerPlayer.
     * 
     * range [0, inf]
     * default 60
     */
    default int maxTrainersTotal() { return 60; }

    /**
     * The maximum level difference between the strongest pokemon in the team of a
     * player and the strongest pokemon in the team of a trainer to spawn for that
     * player. The spawn weight decreases with a higher level difference. Trainers with
     * pokemon above the level cap of a player are excluded.
     * 
     * range [0, 100]
     * default 25
     */
    default int maxLevelDiff() { return 25; }

    /**
     * A comma separated list of dimensions (e.g. ["multiworld:spawn" , "minecraft:the_end"]).
     * In these dimensions trainers will never spawn.
     *
     * default: []
     */
    default List<? extends String> dimensionBlacklist() { return List.of(); }

    /**
     * A comma separated list of dimensions (e.g. ["multiworld:spawn" , "minecraft:the_end"]).
     * Trainers may only spawn in these dimensions (unless the list is empty).
     *
     * default: []
     */
    default List<? extends String> dimensionWhitelist() { return List.of(); }

    /**
     * A comma separated list of biome tags (e.g. ["is_overworld", "is_forest"]). A
     * biome may not have any of the given tags attached to it, for a trainer to spawn
     * in that biome. Trainers may also have additional tags defined by a data pack.
     * 
     * default: []
     */
    default List<? extends String> biomeTagBlacklist() { return List.of(); }

    /**
     * A comma separated list of biome tags (e.g. ["is_overworld", "is_forest"]). A
     * biome must have atleast one of the given tags attached to it, for a trainer to
     * spawn in that biome (unless the list is empty). Trainers may also have
     * additional tags defined by a data pack.
     * 
     * default: []
     */
    default List<? extends String> biomeTagWhitelist() { return List.of(); }

    /**
     * Initial level cap of players. Pokemon will not gain any experience if at or
     * above the level cap.
     * 
     * range [1, 100]
     * default 15
     */
    default int initialLevelCap() { return 15; }

    /**
     * The required level cap for trainers is based of the strongest pokemon in their
     * team. This value will be added to the derived level cap.
     * 
     * Example: A trainer with a Pikachu at level 50 has a level cap requirement of 50.
     * If the additiveLevelCapRequirement is -10 the required level cap of that
     * trainer becomes 40, if it is 10 the level cap requirement becomes 60.
     * 
     * default: 0
     */
    default int additiveLevelCapRequirement() { return 0; }

    /**
     * If enabled the level cap of a players will not prevent their pokemon from
     * gaining experience and leveling up. Trainers will still refuse to battle players
     * that carry pokemon above their level cap!
     * 
     * default: false
     */
    default boolean allowOverLeveling() { return false; }

    /**
     * Can an empty series be considered completed or uncompletable? You tell me. If
     * enabled an empty series will always be considered completed hence rewarding
     * players immediately with a level cap of 100 otherwise the level cap of players
     * will be as configured by initialLevelCap (and additiveLevelCapRequirement). Note
     * that players will start with an empty series by default.
     * 
     * default: false
     */
    default boolean considerEmptySeriesCompleted() { return false; }

    /**
     * If enabled additional information are printed to the log whenever a trainer
     * spawns or despawns.
     * 
     * default false
     */
    default boolean logSpawning() { return false; }
}
