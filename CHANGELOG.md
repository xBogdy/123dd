# Changelog

## [0.8.x-alpha] - 2024-05-21

***Added***

- **79** new (endgame) trainers, which brings the total number up to **735**
- Command: `player get defeats <trainerId> [<player>]`
- Command: `player set defeats <trainerId> [<players>] <value>`
- Command: `trainer get reward_level_cap <trainerId>`
- Command: `trainer get type <trainerId>`
- Item: Trainer Card (open player stats gui with right click, can be crafted)

***Changed***

- Beating a champ now grants a level cap of 100
- Buffed weaker versions of key trainers
- Command: Renamed former `player get defeats ...` to `player get type_defeats <type> [<player>]`
- Removed battle restrictions for some trainer
- Trainer id system (now with proper unique ids). Saved data from previous versions will automatically migrate to the new system. **Any trainers or trainer related data that has been modified with a data- or resource pack must be migrated manually, this is also the case for any (command block) commands that refer to trainers by their old ids**. This can simply be achieved by changing the *trainer part* of the name from an asset file (***groups* stay the same**). The documentaion contains a list of [all changed ids](https://srcmc.gitlab.io/rct/docs/configuration/legacy/).

***Fixed***

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
