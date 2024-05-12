# Changelog

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
