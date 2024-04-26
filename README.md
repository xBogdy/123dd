# Radical Cobblemon Trainers

Over **650** unique and **challenging** trainers, from the Pokemon rom hack *Radical Red* (v3.02), that will spawn naturally in your world.

## Important

This mod, though mostly in a usable state, is currently in an early developement stage. That means there might be bugs and future updates **may** contain changes that will not be compatible with worlds that have been run with previous versions. Be sure to backup your world before updating.

The *greater* the difference inbetween versions the more likely it is for issues to arise, e.g:

|             |     |            |          |
| ----------- | --- | ---------- | -------- |
| 0.5.0       | ->  | 0.5.42     | unlikely |
| 0.5.0       | ->  | 0.6.0      | possible |
| 0.9.0       | ->  | 1.0.0      | likely   |
| 0.9.0-alpha | ->  | 0.9.0-beta | possible |

## Key Features

- Over **650** unique and challenging trainer teams for all level ranges (including gym leaders, elite 4 members, champions and some special ones)
- Custom trainer npc natural spawning system:
  - loosely comparable to the wandering trader but with a significant smaller delay and a higher spawn cap by default
  - ensures uniqueness: Never will you see two trainers with the same name (unless cheated in)
  - matching spawns: trainers that spawn around a palyer will attempt to match the players strength
  - different trainer types will spawn in different biomes
- Custom trainer npc ai (e.g. trainers *stroll* towards players that can battle against them and will either despawn after some time or if beaten)
- Player stats:
  - badge count: increases if a player beats a leader for their first time
  - beaten elite 4 count: increases if a player beats an elite 4 member for their first time
  - beaten champion count: increases if a player beats a champion for their first time
  - level cap: increases after a player has beaten certain trainers (e.g. gym leaders)
- Level cap system:
  - Pokemon at or above the level cap will not gain any exp
  - it is **alot** more likely for trainers around the level cap of a player to spawn
- Advancements:
  - keep track of your progress (e.g. badges) and become a champion
  - some hidden and special advancements (more to come)
  - custom critera trigger for data pack creators: `defeat_count`
- Battle dependencies: Trainers might have requirements to be battled with (e.g. level cap, gym badge count, beaten elite 4, ...)
- Battle reward system powered by loot tables:
  - custom condition for data pack creators: `level_range`
  - custom condition for data pack creators: `not_defeated` (not yet implemented)
- Trainers talk: Based of the context trainers will give different responses in the chat
- And probably some other things that I can't think of right now...

## Balancing

The balancing is based around the original game (Radical Red). Gym leaders must be defeated to increase a players level cap and gain badges. The initial level cap is `15`:

| Trainer        | Reward Level Cap | Requirements             | Type     |
| -------------- | ---------------- | ------------------------ | -------- |
| Brock          | `27`             |                          | Leader   |
| Misty          | `34`             | 1 Badge                  | Leader   |
| Lt. Surge      | `44`             | 2 Badges                 | Leader   |
| Erika          | `47`             | 3 Badges                 | Leader   |
| Giovanni       | `59`             | 4 Badges                 | Leader   |
| Sabrina        | `68`             | 5 Badges                 | Leader   |
| Koga           | `76`             | 6 Badges                 | Leader   |
| Blaine         | `85`             | 7 Badges                 | Leader   |
| *Any Elite 4*  |                  | 8 Badges                 | Elite 4  |
| *Any Champion* | `94`             | 4 beaten Elite 4 members | Champion |
| ???            | `100`            | 1 beaten Champion        | ???      |

> There are some trainers labled as "Leader", "Elite 4" or "Champion" that do not account for the player progress. These are mostly additional trainers from different regions and serve no specific purpose other than beeing tough opponents. They might drop some special loot though.

## Commands

Following commands are available:

- `rctmod`
  - `player`
    - `get` (Op Level: 1)
      - `level_cap [<player>]`
        - Retrieves the current level cap of the given player.
      - `defeats <trainerType> [<player>]`
        - Retrieves the current number of defeats the given player has against the specified trainer type.
    - `set` (Op Level: 2)
      - `level_cap [<players>] <value>`
        - Sets the current level cap of the given players to the specified value.
      - `defeats <trainerType> [<players>] <value>`
        - Sets the current number of defeats the given players have against the specified trainer type to the given value.
  - `trainer`
    - `get` (Op Level: 1)
      - `required_level_cap <trainerId>`
        - Retrieves the required level cap to fight trainer mobs with the given trainer id.
      - `required_defeats <trainerType> <trainerId>`
        - Retrieves the amount of required defeats against trainers from the specified type to fight trainer mobs with the given trainer id.
      - `max_trainer_wins <trainerId>`
        - Retrieves the max amount of wins trainer mobs with the given trainer id can have before they will not accept any more fights.
      - `max_trainer_defeats <trainerId>`
        - Retrieves the max amount of defeats trainer mobs with the given trainer id can have before they will not accept any more fights.
    - `spawn_for [<player>]` (Op Level: 2)
      - Attempts to spawn a trainer mob in the vicinity of the given player (using natural spawning mechanics).
    - `summon <trainerId> [<position>]` (Op Level: 2)
      - Summons the specified trainer mob at the given postion.

## Configuration

Some global aspects of the mod (e.g. spawn interval or spawn cap) can be adjusted to fit a servers needs.

> See `config/rctmod-server.toml` (farbic default) or `saves/YOUR_WORLD/serverconfig/rctmod-server.toml` (forge and fabric).

## Datapack support

Almost all of the data is provided by an internal data pack. **Alot** can be configured by *overshadowing* certain files with an own custom data pack. Please refer to the [wiki](https://gitlab.com/srcmc/rct/mod/-/wikis/home) (wip) for more information.

## Planned features

- More/Better/Fixed assets (Teams, Textures, Mobs, Dialogs, Loot tables, ...)
- ~~Advancements~~
- ~~More user feedback in certain situations (e.g. when a player gains a badge)~~
- ~~Some config options~~
- ~~More commands~~
- ~~Fabric~~

## Dependencies

- [Cobblemon](https://www.curseforge.com/minecraft/mc-mods/cobblemon)
- [CobblemonTrainers](https://www.curseforge.com/minecraft/mc-mods/cobblemontrainers)
- [Forge Config API Port](https://www.curseforge.com/minecraft/mc-mods/forge-config-api-port-fabric) (only Fabric)
