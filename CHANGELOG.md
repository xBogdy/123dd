# Changelog

## [0.13.x-beta] - 2024-12-05

***Added***

- Command: `player get progress [player]`: Outputs a list of all trainers a player has defeated that are part of the required progression
- Command: `player set progress [player(s)] (before|after) <trainerId>`
- Double battles
- Server config `allowOverLeveling`: Specifies if players should be allowed to level their pokemon above their level cap (default: `false`)
- Server config `trainerSpawnerItems`: Allows to specify what items can be used to configure a trainer spawner to summon specific trainers
- Trainer (type) dependent battle rules: Currently allows to set item usage restrictions, leaders and bosses will by default not allow more than `2` item uses
- Trainer Spawner (craftable) block: Can be attuned to spawn different trainers whenever a player is nearby (and the trainer is not already elsewhere), trainers will not move while standing on this block, what items will spawn what trainers can be configured
- `HomePos` block positon tag for trainer npcs: Trainers will stay/move to the defined position, trainers spawned from a Trainer Spawner block will have that block set as their home position (this is also the case for any trainers nearby a matching Trainer Spawner)

***Changed***

- Command: `trainer get required_defeats` now prints a list of required trainers for trainers that are part of the progression
- Core dependency "CobblemonTrainers" is now replaced by "RCTApi"
- Data pack trainer format is now slightly different (see the docs for a full overview of changes)
- Decreased trainer battle cooldown from `30` to `12` seconds (TODO)
- Increased battle attempts against leaders/bosses from `2` to `3` and against other trainers from `3` to `5` (TODO)
- Minor adjustments to spawn rates: Decreased boosted rates for never beaten leaders/bosses but increased the rates for never beaten trainers in general
- Possible spawn locations of leaders and bosses now somewhat match their themes (rather than beeing able to spawn everywhere) (TODO)
- Progression: Added more required trainer fights, e.g. Rocket Admin Archer after Brock and rival fights
- ServerConfig: Renamed `bonusLevelCap` -> `additiveLevelCapRequirement` (works slightly different but effectively serves the same purpose)
- Some pokemon that are originally supposed to mega evolve (i.e. holding key stones) now hold other competetive held items as replacement (until mega evolutions are supported)
- Some trainers might carry items that they may use in battles (e.g. potions) (TODO)
- Trainer Card GUI: Trainers that can be spawned with a trainer spawner will show the item required to do so in their overview page (TODO)
- Trainer defeat counts are now per player, this means different players can now fight the same trainer in a row (trainers will still tend to walk away if defeated/exhausted once)
- Trainers now follow players holding a Trainer Card in their main- or off hand
- Update to Cobblemon 1.6, Minecraft 1.21, Fabric/Neoforge

***Fixed***

- Some oversights in npc ai behaviour (e.g. player tracking not working properly)
- Some pokemon missing held items
- Some pokemon not having the correct regional form
- Trainer Card now glows and renders the arrow regardless if the trainer spawned in naturally or was summoned in any other way (TODO)
- Trainer pokemon sometimes dropping loot, missing trainer pokemon send out and retrieve animation, and probably some other stuff

***Removed***

- Command: `player set level_cap` (see `player set progress` for alternative)
- ServerConfig: `maxOverLevelCap` (too much of a corner case and barely useful, see `allowOverLeveling` and `additiveLevelCapRequirement` for alternatives)

## [0.12.0-beta] - 2024-10-03

***Added***

- Server Config `bonusLevelCap`: Adjust difficulty with a single value
- Support for PKGBadges/CobbleBadges: Leaders will now drop badges from this mod when a player defeats them for their first time
- Support for SimpleTMs: Some trainer types have a chance to drop a random TR
- Trainer Card tracking feature: The Trainer Card will start to glow if the next key trainer (e.g. leader) spawns nearby and render an arrow that will lead towards them
- Trainer textures: These were procedurally generated in advance and mapped to the trainers so that every *trainer identity* has a unique appearance, though some differences might be very subtle for now. Different versions of the same trainer will have the same body but will most likely wear different outfits

***Changed***

- Drastically increased spawn chances for undefeated trainers (with *emphasized* names) and lowered the bonus spawn chance of key trainers (e.g. leaders) gained by players reaching their level cap
- Increased trainer health from `20` to `30`
- Loot table `defeat_count` condition now supports a `comparator` field (`EQUAL`, `SMALLER`, `GREATER` or `MODULO`)
- Overhauled loot tables: Adjusted drop chances and item pools (see this [spreadsheet](https://docs.google.com/spreadsheets/d/10JjXPP1VvcgO1uat_QU2rwqvuxp5wveNq9U3YzwxzjY/edit?usp=sharing) for more info)
- Prevent Trainer Card to open its GUI while holding an item in the other hand
- Trainer Card GUI: Click on discovered trainers in the list to show more information (e.g. biomes the trainer spawns in)
- Trainers now slowly regenerate health (about twice as fast as horses)

***Fixed***

- Advancement defeat count trigger counting all instead of distinct defeats per trainer type

## [0.11.1-alpha] - 2024-08-31

***Fixed***

- Potential battle error for some players caused by missing 'damage source' for battle loot tables

## [0.11.0-alpha] - 2024-08-07

***Changed***

- Adjustments to trainer behavior (AI): Only undefeated key trainers (e.g. leaders) have a high chance to seek out players that can fight them (small chance for others), also trainers are now much stronger drawn towards villages in general and will most of the time mind own businesse (rather than stalking the player)
- Server Config: Increasd default `maxTrainersPerPlayer` from `4` to `8` and `maxTrainersTotal` from `20` to `24` (since trainers should be more spread out due to the changes in they behaviour)
- Trainers can now spawn on (layered) snow blocks
- Update to CobblemonTrainers `0.1.1.11` (**dropped support for earlier versions so make sure to update both!**)

***Fixed***

- Potential desync of player states when switching between worlds (single-/multiplayer) in the same play session

## [0.10.4-alpha] - 2024-07-17

***Fixed***

- Trainer battles staying registered as *active battles* after the trainer npc was killed

## [0.10.3-alpha] - 2024-07-15

***Changed***

- The death of a trainer will now end a battle immediately

***Fixed***

- Possibility of trainers despawning while in battle
- Undefeated trainers not despawning in chunks that are always loaded (e.g. spawn chunks)

## [0.10.2-alpha] - 2024-06-30

***Added***

- A bunch more trainer chatter
- Notification above the hotbar when pokemon at or above the level cap attempt to gain experience

***Changed***

- Server Config: Decreased default `spawnIntervalTicks` from `1200` to `600` (30 sec.) (you may remove the setting from the config file or delete the complete file to update)
- Server Config: Increased default `maxTrainersPerPlayer` from `4` to `6` (you may remove the setting from the config file or delete the complete file to update)
- Trainers capable of fighting now don't despawn unless really far away (i.e. completely unloaded)

***Fixed***

- Fixed trainers *accepting* battles against players that are already in another battle

## [0.10.1-alpha] - 2024-06-24

***Added***

- Command `summon_persistent`: As the name suggests summons a trainer with the `Persistent` tag already set
- Trainer `identity` can now be set per trainer with a data pack and is used instead of the `displayName` to determine if different trainers refer to the same person (if not defined falls back to `displayName`)

***Changed***

- Repurposed 'Champion Lance' as regular trainer

***Fixed***

- `Persistent` trainers despawning on servers

***Removed***

- Hidden advancement for 'Champion Lance' as he is less of a challenge than his elite 4 version

## [0.10.0-alpha] - 2024-06-19

***Changed***

- Adjusted name colors of trainer types: `LEADER`=green, `E4`=blue, `CHAMP`=light_purple, `TEAM_ROCKET`=dark_gray, `BOSS`=gold
- Advancements have been slightly adjusted to reflect the latest changes in the progression (e.g. removed "Earth Badge" and added "Rising Badge")
- Increased spawn chance for (undefeated) key trainers
- Level cap progression now mostly mirrors the progression of the original game (Radical Red): There are now 9 additional bosses that have to be defeated to raise the level cap (refer to your advancement tab for a hint of what to expect next or check the table in the [mod description](https://modrinth.com/mod/rctmod))
- Lowered level cap granted by Erika from `50` to `47`
- Removed "Leader Giovanni" from his duty as 5th gym leader. Now "Leader Clair" grants the 8th badge
- Removed (misleading) `TRAINER` trainer type and added `BOSS` trainer type
- Removed misleading labels (e.g. "Leader") from trainers that do not actually count towards a players progression
- Removed previously given *buff* for some trainers that do not account for the progression anymore
- Repurposed some weaker versions of key trainers as trainers of different *types*
- Slight adjustments to trainer ai behaviour (they now respect personal space a bit more, prefer to hang around in villages and tend to stroll away once done battling)

***Fixed***

- Some key trainers (e.g. elite 4, bosses) not having an increased spawn chance when needed
- Some key trainers beeing able to spawn before they could actually be battled with
- `player set defeats` command failure when used for trainers that have never been interacted with

## [0.9.3-alpha] - 2024-06-18

***Added***

- Loot tables for (placeholder) badge items (see the [source repo](https://gitlab.com/srcmc/rct/mod/-/tree/1.20.1/common/src/main/resources/data/rctmod/loot_tables/trainers/single?ref_type=heads) for the loot tables, they can be overwritten with a data pack)

***Fixed***

- `defeat_count` loot condition not beeing registered
- `player set defeats` command not changing values on the server side

## [0.9.2-alpha] - 2024-06-17

***Changed***

- Removed battle count restriction (max wins/losses) for persistent trainers

## [0.9.1-alpha] - 2024-06-16

***Fixed***

- Hotifx: Potential crash caused by trainer battles

## [0.9.0-alpha] - 2024-06-15

***Added***

- Command `unregister_persistent`: Utility command for server administrators to unregister persistent trainers manually
- Data pack option for trainers to win/loose an infinite amount of times (set `maxTrainerWins` and/or `maxTrainerDefeats` to `0`)
- Defeat count loot condition for data packs (e.g. have trainers drop special loot on first defeat)
- `Persistent` tag for trainers (`0/false` by default). Persistent trainers will **never** despawn and do not count towards the spawn caps. Care must be taken when enabling this tag for trainers since other trainers will not spawn as long as another trainer with the same name exists.

***Changed***

- Reformatted (spawner) debugging output
- Removed redundant `"this"` property of defeat count condition for achievements
- Reworked spawner system: Trainers now despawn when far away/beeing unloaded (similar to hostile mobs)
- Server config: Lowered default `maxHorizontalDistanceToPlayers` from `80` to `70`
- Server config: Lowered default `minHorizontalDistanceToPlayers` from `30` to `25`
- Server config: Lowered default `spawnIntervalTicks` from `2400` to `1200` (1 min)
- Server config: Raised default `maxTrainersTotal` from `15` to `20`

***Fixed***

- Potential crash if debugging is enabled in the server config

***Removed***

- Config option `despawnDelayTicks`

## [0.8.4-alpha] - 2024-06-10

***Added***

- Server Config `logSpawning`: Debugging option to log information about trainer (de)spawning (disabled by default)
- Server Config options `biomeTagBlackList` and `biomeTagWhitelist` as global options for all trainers (in addition to the tags defined per trainer (group) with data packs)

***Fixed***

- Trainers spawning causing a crash with c2me on fabric

## [0.8.3-alpha] - 2024-06-03

***Fixed***

- Trainers on cooldown not beeing counted towards a palyers spawn cap

## [0.8.2-alpha] - 2024-05-31

***Added***

- Trainer types `RIVAL` and `TRAINER`

***Changed***

- Advancements have been reworked, some have been removed, some have been added, some have changed. Advancements will be granted again if a player repeats an action (e.g. beats a previous Leader again). They can also be granted manually with the vanilla `advancement` command.
- Decreased minimum despawn distance (now equal to *spawning config* `minHorizontalDistanceToPlayers` which is `30` by default)
- Trainers that cannot battle anymore now don't count towards a players spawn cap (they still count towards the global spawn cap)

***Fixed***

- "Wrong way to battle" advancement beeing granted for any npc
- Advancements not beeing granted

## [0.8.1-alpha] - 2024-05-29

***Changed***

- Reeneblad temporarily replaced switching moves (like uturn)
- Update to Cobblemon 1.5.2 and CobblemonTrainers 1.1.7

## [0.8.0-alpha] - 2024-05-29

***Added***

- **84** new (endgame) trainers, which brings the total number up to **740**
- Command: `player get defeats <trainerId> [<player>]`
- Command: `player set defeats <trainerId> [<players>] <value>`
- Command: `trainer get reward_level_cap <trainerId>`
- Command: `trainer get type <trainerId>`
- Item: Trainer Card (open player stats gui with right click, can be crafted)

***Changed***

- Beating a champ now grants a level cap of `100`
- Buffed weaker versions of key trainers (elite 4/champs)
- Command: Renamed former `player get defeats ...` to `player get type_defeats <type> [<player>]`
- Decreased battle cooldown from `2000` to `600` ticks
- Increased spawn cap per player from `3` to `4`, decreased spawn interval from `3600` to `2400` ticks and decreased despawn delay from `24000` to `5000` ticks, in short higher spawn rates by default (**changed default values will not be applied to existing configs**)
- Removed battle restrictions for some trainers
- Spawn rates increased for key trainers not beaten by players (the closer a player gets to the level cap the higher the chance to spawn)
- Trainer id system (now with proper unique ids). Saved data from previous versions will automatically migrate to the new system. **Important notes for migration:** Any trainers or trainer related data that has been modified with a **data- or resource pack** must be migrated manually (***groups* stay the same**) by changing the file names to the new ids. Any (**command block**) **commands** that refer to trainers by their old ids must be migrated manually by changing the commands to use the new ids. Any **trainer npcs** from a previous version will have an invalid id, you can let them either despawn, get rid of them otherwise or change the `TrainerId` tag manually using the `data` command. A list of all id changes can be found in the [documentation](https://srcmc.gitlab.io/rct/docs/configuration/legacy/).

***Fixed***

- Many missing team members with different forms (e.g. 'alola')
- Potential log spam for mobs with an invalid trainer id
- Some missing ivs/evs, moves, abilities, etc. for most of the trainers

***Removed***

- Command: `player set defeats <type> [<players>]`

## [0.7.4-alpha] - 2024-05-19

***Fixed***

- **Temporarily** replaced *switching moves* with alternatives (since they cause a crash with Cobblemon 1.5.0 + CobblemonTrainers when switched pokemon get send back out again): uturn -> bugbite, voltswitch -> spark, flipturn -> bubble, teleport -> amnesia, partingshot -> amnesia, batonpass -> amnesia

## [0.7.3-alpha] - 2024-05-16

***Fixed***

- Config option `maxTrainersTotalValue` setting a different config value `maxLevelDiff`
- Missing config option `maxTrainersTotal` (this time actually)
- Wrongly named config options: Removed "Value" suffixes. **Old config values with wrong names will be reverted to their default values with the changed name. Either remove the "Value" suffixes manually before updating or adjust the configs afterwards (if any changes where made).**

## [0.7.2-alpha] - 2024-05-13

***Fixed***

- Incompatibility with some optimization mods on fabric in ssp (e.g. Sodium)

## [0.7.1-alpha] - 2024-05-13

***Fixed***

- Trainers not responding after a battle win

## [0.7.0-alpha] - 2024-05-12

***Added***

- Support for Cobblemon 1.5.0

## [0.6.0-alpha] - 2024-05-11

***Added***

- Client configuration located at `config/rctmod-client.toml`. Options: `showTrainerTypeSymbols = false`, `showTrainerTypeColors = true`
- Server config option `maxOverLevelCap`: Trainers will refuse to battle players that have pokemon in their party with a level greater than the set value + the level cap of the player (default `0`)

***Changed***

- Small adjustments to trainer mob ai: They'll now eventually stop wandering when close to players
- Trainer names are now colored based of their trainer type, i.e. LEADER: green, E4: light purple, CHAMP: golden, TEAM_ROCKET: dark gray, others: white (optional symbols that are appended to the trainer names can be enabled in the **client config**)
- Trainer names are now shown *emphasized* to players that never have beaten them

***Fixed***

- Issue with data packs not beeing correctly synced to players that entered servers without ever having entered a singleplayer world before
- Trainers getting softlocked in battles if player logs out or dies (now counts as trainer win)

## [0.5.4-alpha] - 2024-05-06

***Changed***

- Increased reward level cap of Leader Erika from `47` to `50` (fixes softlock at Leader Giovanni)
- Minor buff to team of Leader Giovanni

***Fixed***

- `trainer get required_level_cap` command returning reward level cap instead

## [0.5.3-alpha] - 2024-05-05

***Fixed***

- Trainer responses kicking players from servers in online-mode

## [0.5.2-alpha] - 2024-05-04

***Added***

- API: added RCTMod.makeBattle (battles are now started from code instead of by invoking the `trainers makebattle` command)

***Changed***

- API: deprecated ChatUtils.makebattle

***Fixed***

- Trainers refusing to battle in case of issues with configurations from CobblemonTrainers

## [0.5.1-alpha] - 2024-05-01

***Added***

- New chat context 'missing_pokemon' -> player has no pokemon capable of fighting in his team

***Changed***

- Renamed trainer mobs `spawnChance` property to `spawnWeightFactor`

***Fixed***

- Trainers counting battles if right clicked without a team (or all pokemon defeated)
- Trainers mobs sometimes not stopping movement at the start of a battle
- `globalSpawnChance` config options not having any effect (changed default from `0.25` to `1.0`, **the config value from existing configs will not be changed**)

## [0.5.0-alpha] - 2024-04-26

***Added***

- Advancements
- Base trainer set (~650 trainers)
- Battle requirements (e.g. level cap or badges)
- Battle rewards (defined by loot tables)
- Custom commands
- Data pack: Loot table condition `level_range`
- Data pack: advancement criteria `defeat_count`
- Data/Resource pack support (textures, trainers, mobs, loot_tables, advancements, dialogs)
- Level cap system (increase by defeating gym leaders)
- Trainer dialog system (different responses based of their situation)
- Trainer spawning system (different trainer types spawn in different biomes + only trainers matching a players strength will spawn in their vicinity)
