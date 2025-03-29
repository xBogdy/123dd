# Changelog

**Important**: Version **0.14** introduces the **series system**. Existing player saves will automatically be assigned to the *radicalred series*. New players will start with the *empty series* and have to pick a series first (see also *#233* Trainer Association).

## [0.14.3-beta] - 2025-03-29

**Changed:**

- *#267* Updated min dependency of `rctapi` to `0.10.15-beta`

**Fixed:**

- *#266* Custom data packs not getting synchronized to clients (a potential cause for various issues in server environments, providing wrong or misleading information to clients)
  - Added Trainer Card loading indicator
  - Synchronization takes place on player log in or after `/reload`

## [0.14.2-beta] - 2025-03-26

**Added:**

- *#265* Special bulk selectors `/all`, `/types/<typeId>` and `/series/<seriesId>` for `set defeats` command

**Changed:**

- *#264* Empty series id in `set series` command: `...` -> `empty`
- *#263* Reworked how player battle statistics are stored
  - **Important:** Migrating persistent data can always be a little bit scary, the automatic migration process is logged and ensures that old data will only be removed after the new data was saved. Regardless creating a backup of the `data` directory can be an additional safety measure
  - Defeat statistics are now batched in groups per file (instead of one file per trainer)
  - This significantly improves loading times (especially for servers with many players and overall greater amounts of defeats against trainers)
- *#262* Trainer Card UI will now open while holding another item in the off hand - if the Trainer Card is in the off hand the UI will only open if the item in the main hand is not in use - blocking with a shield will always prevent the UI from opening

**Fixed:**

- *#261* Clients sometimes being unable to join servers - `invalid stream header` (note: it was difficult to reliably reproduce this issue but I did not run it anymore in any of my tests after this *fix*, thanks to AGGStudios for helping out testing this)
- *#260* Level cap sometimes not updating to `100` in the Trainer Card (without relog) after series completion
- *#259* Series completion message not shown to players that have relogged once after the latest server (re)start
- *#258* Trainer Card including defeat counts of (unlisted) trainers from other series

## [0.14.1-beta] - 2025-03-17

**Changed:**

- *#257* `unbound` series will now be unlocked by completing either `bdsp` or `radicalred`

**Fixed:**

- *#256* Level cap not getting set to `100` after completing a series (without defeating all optional trainers as well)
- *#255* `additiveLevelCapRequirement` setting having no effect for trainers other than the first of a series

## [0.14.0-beta] - 2025-03-14

**Added:**

- *#254* *Luck* stat
  - Every player has a luck stat that influences the quality of loot from trainers
  - Finishing a series (see *#253*) will permanently raise the luck of players based of the difficulty of the series (this can be repeated indefinetely, also for the same series, but with "diminishing returns")
- *#253* *Series* system
  - Every series has its own progression (i.e. chain of key trainers and level caps)
  - Only trainers from the current series of a player will be listed in the trainer card of that player
  - Only trainers from the current series of a player will spawn for that player
  - Players now have a `currentSeries` property (defaults to the *empty series*)
  - Players now have statistics to keep track of completed series (the current and completed series of players can be inspected or modified with commands, see *#246* and *#247*)
- *#252* About ~800 new trainers (see *#240* and *#238*)
- *#251* Advancements for the new series and moved all advancements to their own tab (backround texture by [piiixl](https://piiixl.itch.io/))
- *#250* Command `player add progress (before|after)`: Update progress without resetting current progression (useful for optional/alternative progression paths)
- *#249* Command `player get luck`: Retrieves the accumulated luck of a player (based of how many series the player has completed)
- *#248* Command `player get progress graph`: Generate a visual representation of a players series progression that can be viewed online (powered by PlantUML)
- *#247* Command `player get series [completed]`: Retrieves the current or all completed series of a player
- *#246* Command `player set series <seriesId> [completed <count>]`: Updates the current or completed series of a player
- *#245* Config option: `considerEmptySeriesCompleted`
- *#244* Config option: `spawnTrainerAssociation`
- *#243* Config option: `spawningRequiresTrainerCard`
- *#242* Data pack: Trainer mob properties `series` and `substitutes`, a list of series ids the trainer belongs to (the trainer will belong to every series if empty or not set) and a list of trainers that may serve as replacement in terms of series progression
- *#241* Entity: "Trainer Association" (similar to the Wandering Trader)
  - Offers *trades* (options) for players to start (over) a new series (some offers may require to complete certain series first before they will appear)
  - Offers a Trainer Card trade
  - Only one npc per player can spawn at a time
  - Spawns close to players that carry a trainer card and have either not started or completed a series
  - Spawns nearby players that are close to a decently sized village (3 beds in proximity to a village center)
  - The trainer card will glow and point towards the nearest Trainer Association entity for players that are in the *empty series* or have completed their series
  - Works like a regular minecraft npc (can be summoned with `/summon` and persisted by placing them in a boat or by naming them with a name tag)
- *#240* Series *Brilliant Diamond/Shining Pearl* (bdsp): A casual series from the main line games
- *#239* Series *Radical Red*: All trainers and progression as known from previous versions of this mod
- *#238* Series *Unbound*: Difficult trainers and progression from the (incredible) rom hack "Unbound" (no seriously, play it)

**Changed:**

- *#237* (Re)generated trainer textures for old and new trainers (which had the side effect that many trainers may have a different appearance than before)
- *#236* Adjusted some trainer bag items to match their level range
- *#235* Changed some trainer names to be more friendly for the younger audience
- *#234* Forced crash after several failed attempts of retrieving a trainer id (instead of log spam and potentially other unexpected issues)
- *#233* Improved command suggestions (better filtering)
- *#232* Lowered Trainer Spawner min required distance to players to `2` blocks
- *#231* Name of the next key trainer (scrambled entry) will now briefly be visible in the info screen of the Trainer Card
- *#230* Reworked trainer types: These are now data pack objects with common types being predefined by this mod (e.g. "leader" or "rival")
- *#229* Some advancement ids to better distinguish from the new advancements of the other series
  - `defeat_champion` -> `defeat_champion_terry`
  - `defeat_elite_four` -> `defeat_elite_four_kanto`
- *#228* Trainer Card: Minor cosmetic adjustments (e.g. better handling of long trainer names)
- *#227* Trainer Spawner block can now be *forced* to spawn their trainer **by powering it with redstone** (ignoring any level differences or other conditions to nearby players, this may only fail if a trainer with the same identity already exists in the world or the global spawn cap is full)
- *#226* Updated loot table rates, added a few more items and removed loot tables containing modded items (e.g. `simpletms` or `pkgbadges`)

**Fixed:**

- *#225* Some performance improvements to the trainer card
- *#224* Some random synchronization issues when reloading data packs, changing configs and/or switching between multi- and singleplayer worlds
- *#223* Trainer npcs being incapable of rendering armor (thanks Renari)
- *#222* Trainers being able to be persisted with methods other than `summon_pesistent` or by setting their `Persistent` tag to `1` (e.g. by placing them in a boat)

## [0.13.16-beta] - 2025-01-30

**Changed:**

- *#221* Adjusted comments format in config files

**Fixed:**

- *#220* Scrambled names in trainer card only being 3 characters long
- *#219* Trainers responding with "I'm done" instead of complaining about players cayrrying pokemon above their level cap (if `allowOverleveling` was enabled)

## [0.13.15-beta] - 2025-01-22

**Fixed:**

- *#218* Crashes caused by potential attempts to access config values before they are (re)loaded
- *#217* Some issues with other mods that also rely on *RCTApi* and attempt to (re)load trainers from this mod (i.e. *TBCS*)

## [0.13.14-beta] - 2025-01-21

**Fixed:**

- *#216* Clients sometimes failing to register the `rctmod:player_state` package on neoforge (hopefully)

## [0.13.13-beta] - 2025-01-21

**Changed:**

- *#215* Adjusted how trainer data is (re)loaded (less error prone)
- *#214* Update min requirement of `RCTApi` to version `0.10.7-beta` (further improvements to compatibility with other mods using the same api and some bugfixes)

**Fixed:**

- *#213* *Workaround* to prevent issues with persistent trainer entites sometimes ticking before the trainers have been initialized on fabric
- *#212* Synchronization issues with the TrainerCard when players switched between worlds

## [0.13.12-beta] - 2025-01-20

**Fixed:**

- *#211* Persistent trainers getting removed in the overworld

## [0.13.11-beta] - 2025-01-18

**Fixed:**

- *#210* Progression defeats not being granted in some circumstances on servers

## [0.13.10-beta] - 2025-01-18

**Changed:**

- *#209* Update min requirement of `RCTApi` to version `0.10.6-beta` (better compatibility with mods using the same api)

**Fixed:**

- *#208* Slight adjustments to mod initialization (hopefully fixes *random* crashes on startup with neoforge)

## [0.13.9-beta] - 2025-01-16

**Changed:**

- *#207* Slightly tweaked some values related to natural spawning of trainers
- *#206* Some adjustments to configs to ensure that the initial level cap is always greater or equal to the required level cap of the first trainer and the last rewarded level cap will always be `100`

**Fixed:**

- *#205* Config option `additiveLevelCapRequirement` having unwanted side effects when set to negative values
- *#204* Config option `allowOverleveling` also allowing to battle trainers with pokemon above a players level cap when enabled (use `initialLevelCap` for that)
- *#203* Issue with some config options not updating on clients in some occassions (clients currenlty still need to relog to see changes of required- and rewarded-level-caps of trainers in their trainer card)
- *#202* Some bugs with (un)registration of persistent trainers
- *#201* The *progression set* of players also being allowed to store non required trainers
- *#200* Trainer Card arrow and foil effect not working in multiplayer
- *#199* Trainer Card ignoring persistent trainers
- *#198* Trainer Spawner having a low chance for players that had defeats against the trainer but still required them for their progression

## [0.13.8-beta] - 2025-01-14

**Added:**

- *#197* Server config `dimensionBlacklist` (thanks Vaniron)
- *#196* Server config `dimensionWhitelist`

**Changed:**

- *#195* Added hint to names of "Friendly Coaches" for what EVs their pokemon provide, tweaked their ai
- *#194* Already defeated key trainers are now also "highlighted" in the Trainer Card list when required for the progression
- *#193* Trainer Card arrow has now infinite range (the arrow will spin out of control if the target trainer is in a different dimension)

**Fixed:**

- *#192* Fixed Trainer Spawner Block still taking `globalSpawnChance` and `maxTrainersPerPlayer` into account (see *#180*)
- *#191* Trainer Card not recognizing alternative versions of key trainers no longer being required, if any of the versions has already been defeated (in certain circumstances)

## [0.13.7-beta] - 2025-01-07

**Fixed:**

- *#190* Potential crash with Trainer Card and some backpack mods (hopefully)

## [0.13.6-beta] - 2025-01-06

**Added:**

- *#189* *AFK protection*: Trainers will eventually despawn when nearby players do not move at all for a certain amount of time (~ 12 minutes). Note: This is a bit experimental, let me know if you have trainers unexpectedly disappear in front of your eyes.
- *#188* Server config `globalSpawnChanceMinimum`: Minimum spawn chance for trainers based of how many trainers already spawned for a player
- *#187* Server config `spawnIntervalTicksMaximum`: Maximum spawn interval for trainers based of how many trainers already spawned for a player

**Changed:**

- *#186* Default value of `globalSpawnChance` from `1` to `0.85`
- *#185* Default value of `maxTrainersPerPlayer` from `8` to `12`
- *#184* Default value of `maxTrainersTotal` from `24` to `60`
- *#183* Default value of `spawnIntervalTicks` from `600` to `120`
- *#182* Some adjustments to trainer spawning behaviour, to sum it up: Trainers now spawn much more frequent for players (see *#183*) that have no or only a few trainers around them, trainer encounters should be more common while exploring, yet on the other hand reaching higher numbers of trainers will gradually take more and more time when sticking to the same location (see *#187* and *#188*). **Reminder**: Defeating trainers will free up a players spawn cap!
- *#181* The increase of the spawn weight for key trainers is now based of how close pokemon of players are to that trainers **required level cap** rather than the players **current level cap** (especially helpful if `initialLevelCap` is set to `100`)
- *#180* Trainer Spawner Block now ignores the `globalSpawnChance` and trainers spawned by that block will not count towards a players spawn cap anymore (i.e. can still spawn even if a player has filled up their `maxTrainerPerPlayer`)

**Fixed:**

- *#179* Several issues with npc behavior (as well as some adjustemts)
  - fixed trainers tracking towards the same location in certain situations
  - trainers are now more likely to wander towards an (imaginary) *goal* (if they cannot find a path they will choose a new random goal)
- *#178* Trainer Card ticking for *non local players* on clients (potential source of trouble and incompatibility with other mods, e.g. Trinkets)
- *#177* `additiveLevelCapRequirement` being subtracted from `initialLevelCap` rather than added

## [0.13.5-beta] - 2024-12-28

**Changed:**

- *#176* Renamed loot table `defeat_count` conditions `comparator` values (`EQUAL` -> `==`, `LESS` -> `<`, `<=` (new), `GREATER` -> `>`, `>=` (new), `MODULO` -> `%`), changed default `count` from `0` to `1` (default `comparator` is `==`)

**Fixed:**

- *#175* Custom `comparator` for loot table `defeat_count` condition causing a crash
- *#174* Loot tables of custom data packs not working

## [0.13.4-beta] - 2024-12-27

**Fixed:**

- *#173* Level cap related settings requiring a server restart before changes took effect
- *#172* Setting the `initialLevelCap` to high values also affecting the *required level cap* of trainers, which could prevent trainers from spawning if players don't have pokemon close to that level

## [0.13.3-beta] - 2024-12-24

**Fixed:**

- *#171* "Solution" to prevent issues caused by trainers unexpectedly being removed from a world (e.g. picked up with CarryOn)
- *#170* Some issues with updating config values or reloading data packs while a server is running

## [0.13.2-beta] - 2024-12-24

**Fixed:**

- *#169* Suppressed crash caused by trainers being picked up with CarryOn (temporary solution)

## [0.13.1-beta] - 2024-12-15

**Fixed:**

- *#168* Desync issue with TrainerCard (e.g. by switching dimensions)

## [0.13.0-beta] - 2024-12-11

**Added:**

- *#167* Command: `player get progress [player]`: Outputs a list of all trainers a player has defeated that are part of the required progression
- *#166* Command: `player set progress [player(s)] (before|after) <trainerId>`
- *#165* Double battles
- *#164* Server config `allowOverLeveling`: Specifies if players should be allowed to level their pokemon above their level cap (default: `false`)
- *#163* Server config `trainerSpawnerItems`: Allows to specify what items can be used to configure a trainer spawner to summon specific trainers
- *#162* Trainer (type) dependent battle rules: Currently allows to set item usage restrictions, leaders and bosses will by default not allow more than `2` item uses
- *#161* Trainer Spawner (craftable) block: Can be attuned to spawn different trainers whenever a player is nearby (and the trainer is not already elsewhere), trainers will not move while standing on this block, what items will spawn what trainers can be configured (check out the [documentation](https://srcmc.gitlab.io/rct/docs/gameplay/blocks/#trainer-spawner) for more information)
- *#160* `HomePos` block positon tag for trainer npcs: Trainers will stay/move to the defined position, trainers spawned from a Trainer Spawner block will have that block set as their home position (this is also the case for any trainers nearby a matching Trainer Spawner block)

**Changed:**

- *#159* Advancement defeat count trigger property `trainer_id` now an array `trainer_ids` (of which only one must be defeated for an advancement to be granted)
- *#158* Advancment defeat count trigger now triggers if all trainers of a specified `trainer_type` are defeated if a negative `count` is given
- *#157* Command: `trainer get required_defeats` now prints a list of required trainers
- *#156* Core dependency "CobblemonTrainers" is now replaced by "RCTApi"
- *#155* Data pack trainer format is now slightly different (see the docs for a full overview of changes)
- *#154* Decreased trainer battle cooldown from `30` to `12` seconds
- *#153* Increased battle attempts against leaders from `2` to `3` and against other trainers from `3` to `5`
- *#152* Minor adjustments to spawn rates: Decreased boosted rates for never beaten leaders but increased the rates for never beaten trainers in general
- *#151* Possible spawn locations of leaders now somewhat match their themes (rather than beeing able to spawn everywhere)
- *#150* Progression: Added more required trainer fights, e.g. Rocket Admin Archer after Brock and all the rival fights
- *#149* ServerConfig: Renamed `bonusLevelCap` -> `additiveLevelCapRequirement` (works slightly different but effectively serves the same purpose)
- *#148* Some pokemon that are originally supposed to mega evolve (i.e. holding key stones) now hold other competetive held items as replacement (until mega evolutions are supported)
- *#147* Some trainers might carry items that they may use in battles (e.g. potions)
- *#146* Trainer Card GUI: Trainers that can be spawned with a trainer spawner will show the item(s) required to do so on the 'Spawning' info page
- *#145* Trainer defeat counts are now per player, this means different players can now fight the same trainer in a row (trainers will still tend to walk away if defeated/exhausted once)
- *#144* Trainers now follow players holding a Trainer Card in their main- or off hand
- *#143* Update to Cobblemon `1.6`, Minecraft `1.21.1`, Fabric/Neoforge
- *#142* `RIVAL` names are now colored gold

**Fixed:**

- *#141* Some oversights in npc ai behaviour (e.g. player tracking not working properly)
- *#140* Some pokemon missing held items
- *#139* Some pokemon not having the correct regional form
- *#138* Trainer Card now glows and renders the arrow regardless if the trainer spawned in naturally or was summoned in any other way
- *#137* Trainer pokemon sometimes dropping loot, missing trainer pokemon send out and retrieve animations, and probably some other stuff

**Removed:**

- *#136* Command: `player set level_cap` (see `player set progress` for alternative)
- *#135* ServerConfig: `maxOverLevelCap` (too much of a corner case and barely useful, see `allowOverLeveling` and `additiveLevelCapRequirement` for alternatives)
- *#134* Trainer type `BOSS` (former bosses are now either of type `RIVAL` or `TEAM_ROCKET`)

## [0.12.0-beta] - 2024-10-03

**Added:**

- *#133* Server Config `bonusLevelCap`: Adjust difficulty with a single value
- *#132* Support for PKGBadges/CobbleBadges: Leaders will now drop badges from this mod when a player defeats them for their first time
- *#131* Support for SimpleTMs: Some trainer types have a chance to drop a random TR
- *#130* Trainer Card tracking feature: The Trainer Card will start to glow if the next key trainer (e.g. leader) spawns nearby and render an arrow that will lead towards them
- *#129* Trainer textures: These were procedurally generated in advance and mapped to the trainers so that every *trainer identity* has a unique appearance, though some differences might be very subtle for now. Different versions of the same trainer will have the same body but will most likely wear different outfits

**Changed:**

- *#128* Drastically increased spawn chances for undefeated trainers (with *emphasized* names) and lowered the bonus spawn chance of key trainers (e.g. leaders) gained by players reaching their level cap
- *#127* Increased trainer health from `20` to `30`
- *#126* Loot table `defeat_count` condition now supports a `comparator` field (`EQUAL`, `SMALLER`, `GREATER` or `MODULO`)
- *#125* Overhauled loot tables: Adjusted drop chances and item pools (see this [spreadsheet](https://docs.google.com/spreadsheets/d/10JjXPP1VvcgO1uat_QU2rwqvuxp5wveNq9U3YzwxzjY/edit?usp=sharing) for more info)
- *#124* Prevent Trainer Card to open its GUI while holding an item in the other hand
- *#123* Trainer Card GUI: Click on discovered trainers in the list to show more information (e.g. biomes the trainer spawns in)
- *#122* Trainers now slowly regenerate health (about twice as fast as horses)

**Fixed:**

- *#121* Advancement defeat count trigger counting all instead of distinct defeats per trainer type

## [0.11.1-alpha] - 2024-08-31

**Fixed:**

- *#120* Potential battle error for some players caused by missing 'damage source' for battle loot tables

## [0.11.0-alpha] - 2024-08-07

**Changed:**

- *#119* Adjustments to trainer behavior (AI): Only undefeated key trainers (e.g. leaders) have a high chance to seek out players that can fight them (small chance for others), also trainers are now much stronger drawn towards villages in general and will most of the time mind own businesse (rather than stalking the player)
- *#118* Server Config: Increasd default `maxTrainersPerPlayer` from `4` to `8` and `maxTrainersTotal` from `20` to `24` (since trainers should be more spread out due to the changes in they behaviour)
- *#117* Trainers can now spawn on (layered) snow blocks
- *#116* Update to CobblemonTrainers `0.1.1.11` (**dropped support for earlier versions so make sure to update both!**)

**Fixed:**

- *#115* Potential desync of player states when switching between worlds (single-/multiplayer) in the same play session

## [0.10.4-alpha] - 2024-07-17

**Fixed:**

- *#114* Trainer battles staying registered as *active battles* after the trainer npc was killed

## [0.10.3-alpha] - 2024-07-15

**Changed:**

- *#113* The death of a trainer will now end a battle immediately

**Fixed:**

- *#112* Possibility of trainers despawning while in battle
- *#111* Undefeated trainers not despawning in chunks that are always loaded (e.g. spawn chunks)

## [0.10.2-alpha] - 2024-06-30

**Added:**

- *#110* A bunch more trainer chatter
- *#109* Notification above the hotbar when pokemon at or above the level cap attempt to gain experience

**Changed:**

- *#108* Server Config: Decreased default `spawnIntervalTicks` from `1200` to `600` (30 sec.) (you may remove the setting from the config file or delete the complete file to update)
- *#107* Server Config: Increased default `maxTrainersPerPlayer` from `4` to `6` (you may remove the setting from the config file or delete the complete file to update)
- *#106* Trainers capable of fighting now don't despawn unless really far away (i.e. completely unloaded)

**Fixed:**

- *#105* Fixed trainers *accepting* battles against players that are already in another battle

## [0.10.1-alpha] - 2024-06-24

**Added:**

- *#104* Command `summon_persistent`: As the name suggests summons a trainer with the `Persistent` tag already set
- *#103* Trainer `identity` can now be set per trainer with a data pack and is used instead of the `displayName` to determine if different trainers refer to the same person (if not defined falls back to `displayName`)

**Changed:**

- *#102* Repurposed 'Champion Lance' as regular trainer

**Fixed:**

- *#101* `Persistent` trainers despawning on servers

**Removed:**

- *#100* Hidden advancement for 'Champion Lance' as he is less of a challenge than his elite 4 version

## [0.10.0-alpha] - 2024-06-19

**Changed:**

- *#99* Adjusted name colors of trainer types: `LEADER`=green, `E4`=blue, `CHAMP`=light_purple, `TEAM_ROCKET`=dark_gray, `BOSS`=gold
- *#98* Advancements have been slightly adjusted to reflect the latest changes in the progression (e.g. removed "Earth Badge" and added "Rising Badge")
- *#97* Increased spawn chance for (undefeated) key trainers
- *#96* Level cap progression now mostly mirrors the progression of the original game (Radical Red): There are now 9 additional bosses that have to be defeated to raise the level cap (refer to your advancement tab for a hint of what to expect next or check the table in the [mod description](https://modrinth.com/mod/rctmod))
- *#95* Lowered level cap granted by Erika from `50` to `47`
- *#94* Removed "Leader Giovanni" from his duty as 5th gym leader. Now "Leader Clair" grants the 8th badge
- *#93* Removed (misleading) `TRAINER` trainer type and added `BOSS` trainer type
- *#92* Removed misleading labels (e.g. "Leader") from trainers that do not actually count towards a players progression
- *#91* Removed previously given *buff* for some trainers that do not account for the progression anymore
- *#90* Repurposed some weaker versions of key trainers as trainers of different *types*
- *#89* Slight adjustments to trainer ai behaviour (they now respect personal space a bit more, prefer to hang around in villages and tend to stroll away once done battling)

**Fixed:**

- *#88* Some key trainers (e.g. elite 4, bosses) not having an increased spawn chance when needed
- *#87* Some key trainers beeing able to spawn before they could actually be battled with
- *#86* `player set defeats` command failure when used for trainers that have never been interacted with

## [0.9.3-alpha] - 2024-06-18

**Added:**

- *#85* Loot tables for (placeholder) badge items (see the [source repo](https://gitlab.com/srcmc/rct/mod/-/tree/1.20.1/common/src/main/resources/data/rctmod/loot_tables/trainers/single?ref_type=heads) for the loot tables, they can be overwritten with a data pack)

**Fixed:**

- *#84* `defeat_count` loot condition not beeing registered
- *#83* `player set defeats` command not changing values on the server side

## [0.9.2-alpha] - 2024-06-17

**Changed:**

- *#82* Removed battle count restriction (max wins/losses) for persistent trainers

## [0.9.1-alpha] - 2024-06-16

**Fixed:**

- *#81* Hotifx: Potential crash caused by trainer battles

## [0.9.0-alpha] - 2024-06-15

**Added:**

- *#80* Command `unregister_persistent`: Utility command for server administrators to unregister persistent trainers manually
- *#79* Data pack option for trainers to win/loose an infinite amount of times (set `maxTrainerWins` and/or `maxTrainerDefeats` to `0`)
- *#78* Defeat count loot condition for data packs (e.g. have trainers drop special loot on first defeat)
- *#77* `Persistent` tag for trainers (`0/false` by default). Persistent trainers will **never** despawn and do not count towards the spawn caps. Care must be taken when enabling this tag for trainers since other trainers will not spawn as long as another trainer with the same name exists.

**Changed:**

- *#76* Reformatted (spawner) debugging output
- *#75* Removed redundant `"this"` property of defeat count condition for achievements
- *#74* Reworked spawner system: Trainers now despawn when far away/beeing unloaded (similar to hostile mobs)
- *#73* Server config: Lowered default `maxHorizontalDistanceToPlayers` from `80` to `70`
- *#72* Server config: Lowered default `minHorizontalDistanceToPlayers` from `30` to `25`
- *#71* Server config: Lowered default `spawnIntervalTicks` from `2400` to `1200` (1 min)
- *#70* Server config: Raised default `maxTrainersTotal` from `15` to `20`

**Fixed:**

- *#69* Potential crash if debugging is enabled in the server config

**Removed:**

- *#68* Config option `despawnDelayTicks`

## [0.8.4-alpha] - 2024-06-10

**Added:**

- *#67* Server Config `logSpawning`: Debugging option to log information about trainer (de)spawning (disabled by default)
- *#66* Server Config options `biomeTagBlackList` and `biomeTagWhitelist` as global options for all trainers (in addition to the tags defined per trainer (group) with data packs)

**Fixed:**

- *#65* Trainers spawning causing a crash with c2me on fabric

## [0.8.3-alpha] - 2024-06-03

**Fixed:**

- *#64* Trainers on cooldown not beeing counted towards a palyers spawn cap

## [0.8.2-alpha] - 2024-05-31

**Added:**

- *#63* Trainer types `RIVAL` and `TRAINER`

**Changed:**

- *#62* Advancements have been reworked, some have been removed, some have been added, some have changed. Advancements will be granted again if a player repeats an action (e.g. beats a previous Leader again). They can also be granted manually with the vanilla `advancement` command.
- *#61* Decreased minimum despawn distance (now equal to *spawning config* `minHorizontalDistanceToPlayers` which is `30` by default)
- *#60* Trainers that cannot battle anymore now don't count towards a players spawn cap (they still count towards the global spawn cap)

**Fixed:**

- *#59* "Wrong way to battle" advancement beeing granted for any npc
- *#58* Advancements not beeing granted

## [0.8.1-alpha] - 2024-05-29

**Changed:**

- *#57* Reeneblad temporarily replaced switching moves (like uturn)
- *#56* Update to Cobblemon 1.5.2 and CobblemonTrainers 1.1.7

## [0.8.0-alpha] - 2024-05-29

**Added:**

- *#55* **84** new (endgame) trainers, which brings the total number up to **740**
- *#54* Command: `player get defeats <trainerId> [<player>]`
- *#53* Command: `player set defeats <trainerId> [<players>] <value>`
- *#52* Command: `trainer get reward_level_cap <trainerId>`
- *#51* Command: `trainer get type <trainerId>`
- *#50* Item: Trainer Card (open player stats gui with right click, can be crafted)

**Changed:**

- *#49* Beating a champ now grants a level cap of `100`
- *#48* Buffed weaker versions of key trainers (elite 4/champs)
- *#47* Command: Renamed former `player get defeats ...` to `player get type_defeats <type> [<player>]`
- *#46* Decreased battle cooldown from `2000` to `600` ticks
- *#45* Increased spawn cap per player from `3` to `4`, decreased spawn interval from `3600` to `2400` ticks and decreased despawn delay from `24000` to `5000` ticks, in short higher spawn rates by default (**changed default values will not be applied to existing configs**)
- *#44* Removed battle restrictions for some trainers
- *#43* Spawn rates increased for key trainers not beaten by players (the closer a player gets to the level cap the higher the chance to spawn)
- *#42* Trainer id system (now with proper unique ids). Saved data from previous versions will automatically migrate to the new system. **Important notes for migration:** Any trainers or trainer related data that has been modified with a **data- or resource pack** must be migrated manually (***groups* stay the same**) by changing the file names to the new ids. Any (**command block**) **commands** that refer to trainers by their old ids must be migrated manually by changing the commands to use the new ids. Any **trainer npcs** from a previous version will have an invalid id, you can let them either despawn, get rid of them otherwise or change the `TrainerId` tag manually using the `data` command. A list of all id changes can be found in the [documentation](https://srcmc.gitlab.io/rct/docs/configuration/legacy/).

**Fixed:**

- *#41* Many missing team members with different forms (e.g. 'alola')
- *#40* Potential log spam for mobs with an invalid trainer id
- *#39* Some missing ivs/evs, moves, abilities, etc. for most of the trainers

**Removed:**

- *#38* Command: `player set defeats <type> [<players>]`

## [0.7.4-alpha] - 2024-05-19

**Fixed:**

- *#37* **Temporarily** replaced *switching moves* with alternatives (since they cause a crash with Cobblemon 1.5.0 + CobblemonTrainers when switched pokemon get send back out again): uturn -> bugbite, voltswitch -> spark, flipturn -> bubble, teleport -> amnesia, partingshot -> amnesia, batonpass -> amnesia

## [0.7.3-alpha] - 2024-05-16

**Fixed:**

- *#36* Config option `maxTrainersTotalValue` setting a different config value `maxLevelDiff`
- *#35* Missing config option `maxTrainersTotal` (this time actually)
- *#34* Wrongly named config options: Removed "Value" suffixes. **Old config values with wrong names will be reverted to their default values with the changed name. Either remove the "Value" suffixes manually before updating or adjust the configs afterwards (if any changes where made).**

## [0.7.2-alpha] - 2024-05-13

**Fixed:**

- *#33* Incompatibility with some optimization mods on fabric in ssp (e.g. Sodium)

## [0.7.1-alpha] - 2024-05-13

**Fixed:**

- *#32* Trainers not responding after a battle win

## [0.7.0-alpha] - 2024-05-12

**Added:**

- *#31* Support for Cobblemon 1.5.0

## [0.6.0-alpha] - 2024-05-11

**Added:**

- *#30* Client configuration located at `config/rctmod-client.toml`. Options: `showTrainerTypeSymbols = false`, `showTrainerTypeColors = true`
- *#29* Server config option `maxOverLevelCap`: Trainers will refuse to battle players that have pokemon in their party with a level greater than the set value + the level cap of the player (default `0`)

**Changed:**

- *#28* Small adjustments to trainer mob ai: They'll now eventually stop wandering when close to players
- *#27* Trainer names are now colored based of their trainer type, i.e. LEADER: green, E4: light purple, CHAMP: golden, TEAM_ROCKET: dark gray, others: white (optional symbols that are appended to the trainer names can be enabled in the **client config**)
- *#26* Trainer names are now shown *emphasized* to players that never have beaten them

**Fixed:**

- *#25* Issue with data packs not beeing correctly synced to players that entered servers without ever having entered a singleplayer world before
- *#24* Trainers getting softlocked in battles if player logs out or dies (now counts as trainer win)

## [0.5.4-alpha] - 2024-05-06

**Changed:**

- *#23* Increased reward level cap of Leader Erika from `47` to `50` (fixes softlock at Leader Giovanni)
- *#22* Minor buff to team of Leader Giovanni

**Fixed:**

- *#21* `trainer get required_level_cap` command returning reward level cap instead

## [0.5.3-alpha] - 2024-05-05

**Fixed:**

- *#20* Trainer responses kicking players from servers in online-mode

## [0.5.2-alpha] - 2024-05-04

**Added:**

- *#19* API: added RCTMod.makeBattle (battles are now started from code instead of by invoking the `trainers makebattle` command)

**Changed:**

- *#18* API: deprecated ChatUtils.makebattle

**Fixed:**

- *#17* Trainers refusing to battle in case of issues with configurations from CobblemonTrainers

## [0.5.1-alpha] - 2024-05-01

**Added:**

- *#16* New chat context 'missing_pokemon' -> player has no pokemon capable of fighting in his team

**Changed:**

- *#15* Renamed trainer mobs `spawnChance` property to `spawnWeightFactor`

**Fixed:**

- *#14* Trainers counting battles if right clicked without a team (or all pokemon defeated)
- *#13* Trainers mobs sometimes not stopping movement at the start of a battle
- *#12* `globalSpawnChance` config options not having any effect (changed default from `0.25` to `1.0`, **the config value from existing configs will not be changed**)

## [0.5.0-alpha] - 2024-04-26

**Added:**

- *#11* Advancements
- *#10* Base trainer set (~650 trainers)
- *#9* Battle requirements (e.g. level cap or badges)
- *#8* Battle rewards (defined by loot tables)
- *#7* Custom commands
- *#6* Data pack: Loot table condition `level_range`
- *#5* Data pack: advancement criteria `defeat_count`
- *#4* Data/Resource pack support (textures, trainers, mobs, loot_tables, advancements, dialogs)
- *#3* Level cap system (increase by defeating gym leaders)
- *#2* Trainer dialog system (different responses based of their situation)
- *#1* Trainer spawning system (different trainer types spawn in different biomes + only trainers matching a players strength will spawn in their vicinity)
