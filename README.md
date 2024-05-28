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

- Over **700** unique and challenging trainer teams for all level ranges (including gym leaders, elite 4 members, champions and some special ones)
- Custom trainer npc natural spawning system:
  - loosely comparable to the wandering trader but with a significant smaller delay and a higher spawn cap by default
  - ensures uniqueness: Never will you see two trainers with the same name (unless cheated in)
  - matching spawns: trainers that spawn around a palyer will attempt to match the players strength
  - different trainer types will spawn in different biomes
- Custom trainer npc ai (e.g. trainers *stroll* towards players that can battle against them and will either despawn after some time or if beaten)
- Player stats:
  - badge count*: increases if a player beats a leader for their first time
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
  - custom condition for data pack creators: `defeat_count` (not yet implemented)
- Trainers talk: Based of the context trainers will give different responses in the chat
- And probably some other things that I can't think of right now...

> The mod itself does not provide any badge items but rather counts the wins against *leaders*. When a leader is beaten for the first time an advancement for the badge is granted.

## Balancing

The balancing is based around the original game (Radical Red). Gym leaders must be defeated to increase a players level cap and *gain* badges. The initial level cap is `15`:

| Trainer        | Reward Level Cap | Requirements             | Type     |
| -------------- | ---------------- | ------------------------ | -------- |
| Brock          | `27`             |                          | Leader   |
| Misty          | `34`             | 1 Badge                  | Leader   |
| Lt. Surge      | `44`             | 2 Badges                 | Leader   |
| Erika          | `50`             | 3 Badges                 | Leader   |
| Giovanni       | `59`             | 4 Badges                 | Leader   |
| Sabrina        | `68`             | 5 Badges                 | Leader   |
| Koga           | `76`             | 6 Badges                 | Leader   |
| Blaine         | `85`             | 7 Badges                 | Leader   |
| *Any Elite 4*  |                  | 8 Badges                 | Elite 4  |
| *Any Champion* | `100`            | 4 beaten Elite 4 members | Champion |

> There are some trainers labled as "Leader", "Elite 4" or "Champion" that do not account for the player progress. These are mostly additional trainers from different regions and serve no specific purpose other than beeing tough opponents. They might drop some special loot though.

## Trainer Card

Keep track of your progress:

[!trainer_card](trainer_card_image.png)

> Trainers are sorted by strength (weaker first) > name > trainer id (not shown).

[!trainer_card](trainer_card_recipe.png)

> **The item itself does not store any information, hence any player will see their own stats with any trainer card.** This *might* be something to be added in future versions.

## Commands

Following commands are available:

- `rctmod`
  - `player`
    - `get` (Op Level: 1)
      - `level_cap [<player>]`
        - Retrieves the current level cap of the given player.
      - `defeats <trainerId> [<player>]`
        - Retrieves the current number of defeats the given player has against the specified trainer.
      - `type_defeats <trainerType> [<player>]`
        - Retrieves the current number of defeats the given player has against the specified trainer type.
    - `set` (Op Level: 2)
      - `level_cap [<players>] <value>`
        - Sets the current level cap of the given players to the specified value.
      - `defeats <trainerId> [<players>] <value>`
        - Sets the current number of defeats the given players have against the specified trainer to the given value.
  - `trainer`
    - `get` (Op Level: 1)
      - `type <trainerId>`
        - Retrieves the type of the trainer with the given trainer id.
      - `reward_level_cap <trainerId>`
        - Retrieves the reward level cap trainer mobs with the given trainer id will grant to players if beaten.
      - `required_level_cap <trainerId>`
        - Retrieves the required level cap to fight trainer mobs with the given trainer id.
      - `required_defeats <trainerType> <trainerId>`
        - Retrieves the amount of required defeats against trainers from the specified type to fight trainer mobs with the given trainer id.
      - `max_trainer_wins <trainerId>`
        - Retrieves the max amount of wins trainer mobs with the given trainer id can have before they will not accept any more fights.
      - `max_trainer_defeats <trainerId>`
        - Retrieves the max amount of defeats trainer mobs with the given trainer id can have before they will not accept any more fights.
    - `spawn_for [<player>]` (Op Level: 2)
      - Attempts to spawn a trainer mob in the vicinity of the given player (using the custom natural spawning mechanics).
    - `summon <trainerId> [<position>]` (Op Level: 2)
      - Summons the specified trainer mob at the given postion.

## Configuration

This mod provides many config options - "initial level cap" and "max over level cap" or "spawn interval" and "spawn cap" just to name a few. The [doumentation](https://srcmc.gitlab.io/rct/docs/configuration/0_server_config/) contains a table of all available config options.

## Datapack support

Almost all of the data is provided by an internal data pack. **Alot** can be configured by *overshadowing* certain files with an own custom data pack. Please refer to the [documentation](https://srcmc.gitlab.io/rct/docs/configuration/1_fallback_data_system/) for more information.

## Planned features

- More/Better/Fixed assets (Teams, Textures, Mobs, Dialogs, Loot tables, ...)
- More config and datapack options to allow trainers beeing used in advancement like scenarios:
  - disable despawning
  - max trainer defeats/wins per player
  - infinite trainer defeats/wins (e.g. by setting (global) max wins/defeats to `0`)
  - loot condition for n'th (e.g. 1st) defeat (`defeat_count`)
- ~~Advancements~~
- ~~More user feedback in certain situations (e.g. when a player gains a badge)~~
- ~~Some config options~~
- ~~More commands~~
- ~~Fabric~~

## Dependencies

- [Cobblemon](https://www.curseforge.com/minecraft/mc-mods/cobblemon)
- [CobblemonTrainers](https://www.curseforge.com/minecraft/mc-mods/cobblemontrainers)
- [Forge Config API Port](https://www.curseforge.com/minecraft/mc-mods/forge-config-api-port-fabric) (only Fabric)
