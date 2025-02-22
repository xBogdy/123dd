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
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.Item;

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
     * default 120
     */
    default int spawnIntervalTicks() { return 120; }

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
     * default: 800
     */
    default int spawnIntervalTicksMaximum() { return 1200; }

    /**
     * If enabled trainer association npcs may spawn naturally nearby players that
     * carry a trainer card and have not started a series, completed their current
     * series or are in proximity to a village (at least 3 occupied beds and a village
     * center). These can spawn everywhere but will respect the 'dimensionBlacklist'
     * and 'dimensionWhitelist' settings.
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
     * A list of items that can be used to configure a trainer spawner to spawn
     * specific trainers. Every entry must define an item followed by a space seperated
     * list of trainer ids (of which one will be randomly chosen to spawn).
     * 
     * default: [
     *  "cobblemon:hard_stone" "leader_brock_019e",
     *  "cobblemon:black_tumblestone" "rocket_admin_archer_002e",
     *  "minecraft:gold_nugget" "rival_terry_014c" "rival_terry_014d" "rival_terry_014e",
     *  "cobblemon:mystic_water" "leader_misty_019f",
     *  "cobblemon:silk_scarf" "trainer_brendan_0032",
     *  "cobblemon:magnet" "leader_lt_surge_01a0",
     *  "cobblemon:miracle_seed" "leader_erika_01a1",
     *  "cobblemon:upgrade" "boss_giovanni_015c",
     *  "cobblemon:soothe_bell" "rival_terry_01b0" "rival_terry_01b1" "rival_terry_01b2",
     *  "cobblemon:black_sludge" "rocket_admin_archer_ariana_m000",
     *  "cobblemon:dubious_disc" "boss_giovanni_015d",
     *  "cobblemon:twisted_spoon" "leader_sabrina_01a4",
     *  "cobblemon:expert_belt" "trainer_brendan_0039",
     *  "cobblemon:poison_barb" "leader_koga_01a2",
     *  "cobblemon:vivichoke" "trainer_may_003d",
     *  "cobblemon:charcoal_stick" "leader_blaine_01a3",
     *  "cobblemon:covert_cloak" "rocket_admin_archer_0043",
     *  "cobblemon:utility_umbrella" "rocket_admin_ariana_0044",
     *  "cobblemon:destiny_knot" "boss_giovanni_0045",
     *  "cobblemon:dragon_scale" "leader_clair_004a",
     *  "cobblemon:lucky_egg" "rival_terry_01b3" "rival_terry_01b4" "rival_terry_01b5",
     *  "cobblemon:choice_scarf" "trainer_brendan_001a",
     *  "cobblemon:cleanse_tag" "elite_four_agatha_0053" "elite_four_agatha_0054",
     *  "cobblemon:focus_band" "elite_four_bruno_0050" "elite_four_bruno_0051",
     *  "cobblemon:dragon_fang" "elite_four_lance_0056" "elite_four_lance_0057",
     *  "cobblemon:never_melt_ice" "elite_four_lorelei_004d" "elite_four_lorelei_004e",
     *  "cobblemon:life_orb" "champion_terry_01b6" "champion_terry_01b7" "champion_terry_01b8"
     * ]
     */
    default Map<String, List<String>> trainerSpawnerItems() {
        return Map.<String, List<String>>ofEntries(
            Map.<String, List<String>>entry("cobblemon:hard_stone", List.of("leader_brock_019e")),
            Map.<String, List<String>>entry("cobblemon:black_tumblestone", List.of("rocket_admin_archer_002e")),
            Map.<String, List<String>>entry("minecraft:gold_nugget", List.of("rival_terry_014c", "rival_terry_014d", "rival_terry_014e")),
            Map.<String, List<String>>entry("cobblemon:mystic_water", List.of("leader_misty_019f")),
            Map.<String, List<String>>entry("cobblemon:silk_scarf", List.of("trainer_brendan_0032")),
            Map.<String, List<String>>entry("cobblemon:magnet", List.of("leader_lt_surge_01a0")),
            Map.<String, List<String>>entry("cobblemon:miracle_seed", List.of("leader_erika_01a1")),
            Map.<String, List<String>>entry("cobblemon:upgrade", List.of("boss_giovanni_015c")),
            Map.<String, List<String>>entry("cobblemon:soothe_bell", List.of("rival_terry_01b0", "rival_terry_01b1", "rival_terry_01b2")),
            Map.<String, List<String>>entry("cobblemon:black_sludge", List.of("rocket_admin_archer_ariana_m000")),
            Map.<String, List<String>>entry("cobblemon:dubious_disc", List.of("boss_giovanni_015d")),
            Map.<String, List<String>>entry("cobblemon:twisted_spoon", List.of("leader_sabrina_01a4")),
            Map.<String, List<String>>entry("cobblemon:expert_belt", List.of("trainer_brendan_0039")),
            Map.<String, List<String>>entry("cobblemon:poison_barb", List.of("leader_koga_01a2")),
            Map.<String, List<String>>entry("cobblemon:vivichoke", List.of("trainer_may_003d")),
            Map.<String, List<String>>entry("cobblemon:charcoal_stick", List.of("leader_blaine_01a3")),
            Map.<String, List<String>>entry("cobblemon:covert_cloak", List.of("rocket_admin_archer_0043")),
            Map.<String, List<String>>entry("cobblemon:utility_umbrella", List.of("rocket_admin_ariana_0044")),
            Map.<String, List<String>>entry("cobblemon:destiny_knot", List.of("boss_giovanni_0045")),
            Map.<String, List<String>>entry("cobblemon:dragon_scale", List.of("leader_clair_004a")),
            Map.<String, List<String>>entry("cobblemon:lucky_egg", List.of("rival_terry_01b3", "rival_terry_01b4", "rival_terry_01b5")),
            Map.<String, List<String>>entry("cobblemon:choice_scarf", List.of("trainer_brendan_001a")),
            Map.<String, List<String>>entry("cobblemon:cleanse_tag", List.of("elite_four_agatha_0053", "elite_four_agatha_0054")),
            Map.<String, List<String>>entry("cobblemon:focus_band", List.of("elite_four_bruno_0050", "elite_four_bruno_0051")),
            Map.<String, List<String>>entry("cobblemon:dragon_fang", List.of("elite_four_lance_0056", "elite_four_lance_0057")),
            Map.<String, List<String>>entry("cobblemon:never_melt_ice", List.of("elite_four_lorelei_004d", "elite_four_lorelei_004e")),
            Map.<String, List<String>>entry("cobblemon:life_orb", List.of("champion_terry_01b6", "champion_terry_01b7", "champion_terry_01b8"))
        );
    }

    /**
     * Retrieves a set of spawner items that are configured to spawn a trainer with the
     * given trainer id. This values is not a config value but derived from {@link
     * IServerConfig#trainerSpawnerItems()} on config reload.
     */
    default Set<Item> spawnerItemsFor(String trainerId) { return Set.of(); }

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
