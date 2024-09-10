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
package com.gitlab.srcmc.rctmod.api.config;

import java.util.List;

public interface IServerConfig extends IForgeConfig {
    /**
     * A global factor that determines if a spawn attempt for a trainer is made.
     * 
     * range [0, 1]
     * default 1.0
     */
    default double globalSpawnChance() { return 1.0; }

    /**
     * The interval in ticks at which a spawn attempt is made per player.
     * 
     * range [1, inf]
     * default 600
     */
    default int spawnIntervalTicks() { return 600; }

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
     * default 8
     */
    default int maxTrainersPerPlayer() { return 8; }

    /**
     * Total trainer spawn cap.
     * 
     * range [0, inf]
     * default 24
     */
    default int maxTrainersTotal() { return 24; }

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
     * Trainers will refuse to battle players that have pokemon in their party with a
     * level greater than the set value + the level cap of the player. This value can
     * also be negative.
     * 
     * default 0
     */
    default int maxOverLevelCap() { return 0; }

    /**
     * This is your one stop difficulty setting. The 'bonusLevelCap' is added to the
     * 'initialLevelCap' aswell as any increased level cap rewarded by trainers (except
     * of trainers that reward a level cap of 100). In short, a positive value will
     * make this mod easier a negative value harder.
     * 
     * On a side note, trainers will also take this value into account when determining
     * the required level cap to fight them. In practice this matters only for negative
     * values as the required level cap will never be above the level of the strongest
     * pokemon of a trainer. For example if we assume bonusLevelCap=-3: A trainer with
     * a strongest pokemon at level 15 would usually require a level cap of 15, now a
     * level cap of 15-3=12 is required.
     * 
     * default: 0
     */
    default int bonusLevelCap() { return 0; }

    /**
     * If enabled additional information are printed to the log whenever a trainer
     * spawns or despawns.
     * 
     * default false
     */
    default boolean logSpawning() { return false; }
}
