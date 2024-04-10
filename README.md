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
- Custom trainer npc ai (e.g. trainers *stroll* towards players that can battle against them and will either despawn after some time or if beaten)
- Player stats:
  - badge count: increases if a player beats a leader for their first time
  - beaten elite 4 count: increases if a player beats an elite 4 member for their first time
  - beaten champion count: increases if a player beats a champion for their first time
  - level cap: increases after a player has beaten certain trainers (e.g. gym leaders)
- Level cap system:
  - Pokemon at or above the level cap will not gain any exp
  - it is **alot** more likely for trainers around the level cap of a player to spawn
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

> There are some extra leaders that do not grant an increased level cap or badges. It is planned to have such leaders (and other special trainers) reward players with special loot.

## Commands

Following commands are available:

| Command                                                                                                  | Description                                                                                                               | Op Level |
| -------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- | -------- |
| `rctmod player get (level_cap/badges/beaten_e4/beaten_champs) [<player>]`                                | Retrieves either the level cap or the amount of badges, beaten elite 4 or champions from the given player.                | `1`      |
| `rctmod player set (level_cap/badges/beaten_e4/beaten_champs) [<player>] <value>`                        | Sets either the level cap or the amount of badges, beaten elite 4 or champions from the given player to the given value.  | `2`      |
| `rctmod mob get (required_level_cap/required_badges/required_beaten_e4/required_beaten_champs) <entity>` | Retrieves either the required level cap or the amount of required badges, beaten elite 4 or champions from the given mob. | `1`      |
| `rctmod mob get (player_wins/player_defeats) <entity> [<player>]`                                        | Retrieves the amount of wins or defeats the given player has against the given mob.                                       | `1`      |
| `rctmod mob spawn_for [<player>]`                                                                        | Attempts to spawn a trainer mob in the vicinity of the given player (uses natural spawning mechanics).                    | `2`      |
| `rctmod mob summon <trainerId> [<position>]`                                                             | Summons a trainer mob at the given postion.                                                                               | `2`      |

## Configuration

Some global aspects of the mod (e.g. spawn interval or spawn cap) can be adjusted to fit a servers needs.

**Currently not implemented**.

## Datapack support

Almost all of the data is provided by an internal data pack. **Alot** can be configured by *overshadowing* certain files with an own custom data pack. Please refer to the [wiki](todo.com) for more information.

## Planned features

- Advancements
- More user feedback in certain situations (e.g. when a player gains a badge)
- More/Better/Fixed assets (Teams, Textures, Mobs, Dialogs, Loot tables, ...)
- Some config options
- ~~More commands~~
- Fabric

## Dependencies

- [Cobblemon](todo.com)
- [CobblemonTrainers](todo.com)
