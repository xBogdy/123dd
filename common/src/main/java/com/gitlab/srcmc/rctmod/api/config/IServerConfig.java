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
import java.util.Map;

public interface IServerConfig extends IModConfig {
    /**
     * Reload is invoked after the config was reloaded and all config values have been parsed.
     */
    default void reload() {}

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
     * A list of items that can be used to configure a trainer spawner to spawn
     * specific trainers. Every entry must define an item followed by a space seperated
     * list of trainer ids (of which one will be randomly chosen to spawn).
     * 
     * default: [
     *  "cobblemon:hard_stone leader_brock_019e",
     *  "cobblemon:mystic_water leader_misty_019f",
     *  "cobblemon:magnet leader_lt_surge_01a0",
     *  "cobblemon:miracle_seed leader_erika_01a1",
     *  "cobblemon:upgrade boss_giovanni_015c",
     *  "cobblemon:black_sludge rocket_admin_archer_0035 rocket_admin_ariana_0036",
     *  "cobblemon:dubious_disc boss_giovanni_015d",
     *  "cobblemon:twisted_spoon leader_sabrina_01a4",
     *  "cobblemon:poison_barb leader_koga_01a2",
     *  "cobblemon:vivichoke trainer_may_003d",
     *  "cobblemon:charcoal_stick leader_blaine_01a3",
     *  "cobblemon:toxic_orb rocket_admin_archer_0043 rocket_admin_ariana_0044",
     *  "cobblemon:destiny_knot boss_giovanni_0045",
     *  "cobblemon:dragon_fang leader_clair_004a",
     *  "cobblemon:silk_scarf trainer_brendan_001a",
     *  "cobblemon:choice_specs elite_four_agatha_0053", "elite_four_agatha_0054",
     *  "cobblemon:focus_band elite_four_bruno_0050", "elite_four_bruno_0051",
     *  "cobblemon:choice_band elite_four_lance_0056", "elite_four_lance_0057",
     *  "cobblemon:choice_scarf elite_four_lorelei_004d", "elite_four_lorelei_004e",
     *  "cobblemon:life_orb champion_terry_01b6", "champion_terry_01b7", "champion_terry_01b8", "champion_terry_02e3", "champion_terry_02e4", "champion_terry_02e"
     * ]
     */
    default Map<String, List<String>> trainerSpawnerItems() {
        return Map.<String, List<String>>ofEntries(
            Map.<String, List<String>>entry("cobblemon:hard_stone", List.of("leader_brock_019e")),
            Map.<String, List<String>>entry("cobblemon:mystic_water", List.of("leader_misty_019f")),
            Map.<String, List<String>>entry("cobblemon:magnet", List.of("leader_lt_surge_01a0")),
            Map.<String, List<String>>entry("cobblemon:miracle_seed", List.of("leader_erika_01a1")),
            Map.<String, List<String>>entry("cobblemon:upgrade", List.of("boss_giovanni_015c")),
            Map.<String, List<String>>entry("cobblemon:black_sludge", List.of("rocket_admin_archer_0035", "rocket_admin_ariana_0036")), // TODO: double battle
            Map.<String, List<String>>entry("cobblemon:dubious_disc", List.of("boss_giovanni_015d")),
            Map.<String, List<String>>entry("cobblemon:twisted_spoon", List.of("leader_sabrina_01a4")),
            Map.<String, List<String>>entry("cobblemon:poison_barb", List.of("leader_koga_01a2")),
            Map.<String, List<String>>entry("cobblemon:vivichoke", List.of("trainer_may_003d")),
            Map.<String, List<String>>entry("cobblemon:charcoal_stick", List.of("leader_blaine_01a3")),
            Map.<String, List<String>>entry("cobblemon:toxic_orb", List.of("rocket_admin_archer_0043", "rocket_admin_ariana_0044")), // TODO: double battle
            Map.<String, List<String>>entry("cobblemon:destiny_knot", List.of("boss_giovanni_0045")),
            Map.<String, List<String>>entry("cobblemon:dragon_fang", List.of("leader_clair_004a")),
            Map.<String, List<String>>entry("cobblemon:silk_scarf", List.of("trainer_brendan_001a")),
            Map.<String, List<String>>entry("cobblemon:choice_specs", List.of("elite_four_agatha_0053", "elite_four_agatha_0054")),
            Map.<String, List<String>>entry("cobblemon:focus_band", List.of("elite_four_bruno_0050", "elite_four_bruno_0051")),
            Map.<String, List<String>>entry("cobblemon:choice_band", List.of("elite_four_lance_0056", "elite_four_lance_0057")),
            Map.<String, List<String>>entry("cobblemon:choice_scarf", List.of("elite_four_lorelei_004d", "elite_four_lorelei_004e")),
            Map.<String, List<String>>entry("cobblemon:life_orb", List.of("champion_terry_01b6", "champion_terry_01b7", "champion_terry_01b8", "champion_terry_02e3", "champion_terry_02e4", "champion_terry_02e5"))
        );
    }

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
     * The 'bonusLevelCap' is added to the 'initialLevelCap' as well as any increased
     * level cap rewarded by trainers (except of trainers that reward a level cap of
     * 100). In short, a positive value will make this mod easier a negative value
     * harder.
     * 
     * On a side note, trainers will also take this value into account when determining
     * the required level cap to fight them. For example if we assume bonusLevelCap=-3:
     * A trainer with a strongest pokemon at level 15 would usually require a level cap
     * of 15, now a level cap of 15-3=12 is required.
     * 
     * default: 0
     */
    default int bonusLevelCap() { return 0; }

    /**
     * If enabled the level cap of a players will not prevent their pokemon from
     * gaining experience and leveling up. Trainers will still refuse to battle players
     * that carry pokemon above their level cap!
     * 
     * default: false
     */
    default boolean allowOverLeveling() { return false; }

    /**
     * If enabled additional information are printed to the log whenever a trainer
     * spawns or despawns.
     * 
     * default false
     */
    default boolean logSpawning() { return true; } // TODO: default false
}
