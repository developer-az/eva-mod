# Eva Mod 2.0 — Homestead (NeoForge 26.2)

Friendly, immortal biome NPCs with homes, friendship hearts, seasons, birthdays,
errands, mail, and multi-house towns. Built as a solid production base for Minecraft 26.2.

**Targets Minecraft 26.2 + NeoForge 26.2. Current release: 2.0.0.**

> **Outdated:** jars **1.0–1.1.x** are unsupported. They shipped known locate/teleport
> bugs (including landing near Y=0) and incomplete town tooling. Upgrade to **2.0.0+**.

## Features (2.0)

- **7 biome variants** with unique looks, name pools, and jobs.
- **Towns**: uncommon hamlets (`npc_town`) with a plaza and 3–6 houses; solitary `npc_house` remains common.
- **Friendship hearts (0–10)** with milestone heart-event dialogue.
- **Seasons & festivals**: 28-day Spring/Summer/Fall/Winter cycle; festival day mid-season.
- **Birthdays**: every NPC has a calendar birthday; gifts that day mean more.
- **Gift tastes**: love / like / neutral / dislike / hate (personal + job + personality).
- **Errands**: friendly NPCs (2+ hearts) offer Help-wanted fetch quests from dialogue.
- **Mail**: birthday reminders, festival invites, and friendship notes (`/evamod mail`).
- **Journal**: met NPCs with hearts and birthdays (`/evamod journal`).
- **Dialogue + trading UI**: Talk / Trade / Help|Tip / Bye — less repetitive seasonal & gossip lines.
- **Immortal NPCs** with per-player memory, daily routines, and rotating trades.

## Commands

| Command | Cheats? | What it does |
|---------|---------|----------------|
| `/evamod town` | No | Find the nearest **town** (hamlet) and print a **safe interior landing** |
| `/evamod town visit` | Yes | Teleport **inside** a town house (bed / interior — never Y≈0) |
| `/evamod locate` | No | Find next house or town |
| `/evamod visit` | Yes | Teleport into nearest house interior |
| `/evamod journal` | No | Friends, hearts, birthdays |
| `/evamod mail` | No | Read NPC letters |
| `/evamod calendar` | No | Season, date, next festival |
| `/evamod errand` | No | Active help-wanted errand |
| `/evamod near` | No | Nearby NPCs + heart bars |
| `/evamod version` | No | Show 2.0.0 and outdated-1.x notice |

Vanilla `/locate structure evamod:npc_town` is unreliable for this mod’s custom structure type — use **`/evamod town`** instead.

## Building

Requires Java 25 (Minecraft 26.2 toolchain).

```
./gradlew build
```

The mod jar ends up in `build/libs/` as `evamod-2.0.0.jar`.

## Running in dev

```
./gradlew runClient
```

## Regenerating NPC textures

```
python scripts/generate_textures.py
```

## Compatibility

- Registry/network IDs use `evamod`; no global mixins.
- Structure set salts are unique.
- Safe for dedicated multiplayer (server authority for dialogue/trades/memory).
