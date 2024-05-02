# Changelog

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
