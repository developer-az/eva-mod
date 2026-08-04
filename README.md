# Eva Mod — Homestead (NeoForge 26.2)

Friendly, immortal biome NPCs with homes, friendship hearts, seasons, birthdays,
errands, mail, multi-house towns, and a Homestead Primer guide book.

**Targets Minecraft 26.2 + NeoForge 26.2. Current release: 2.1.0.**

> **Outdated:** jars **1.0–1.1.x** are unsupported (locate/teleport bugs). Use **2.0.0+**.
> Saves from 2.0 migrate forward via schema versions.

## Already-explored worlds

Chunks generated **before** Eva Mod never received `npc_house` / `npc_town` pieces.
Natural generation still works in **new** chunks. For a world that was already explored:

1. Every player gets the **Homestead Primer** book once on join (inventory, or dropped at feet).
2. Run **`/evamod settle`** once per overworld to plant a **Founder's Homestead** (one cottage + NPC) near you — no chunk regen, bounded placement.
3. Keep exploring outward for natural towns/houses.

Login never runs structure searches (MP-safe).

## Future-proofing

- Player + world attachments carry **schema versions** and migrate on login (`DataMigrations`).
- Optional codec fields keep old saves loading.
- `EvaContent` holds extension hooks for future errands / guide notes without breaking core.
- Network protocol version lives in `ModVersions.NETWORK`.

## Features

- **7 biome variants**, towns, hearts, seasons/festivals, birthdays, gift tastes, errands, mail, journal
- **Homestead Primer** written book with clickable commands (once per player; `/evamod book` for another)
- **Safe teleports** onto beds / interiors (never Y≈0)
- **`/evamod town`** — dedicated town finder (do not rely on vanilla `/locate structure`)

## Commands

| Command | Cheats? | What it does |
|---------|---------|----------------|
| `/evamod book` | No | Get another Homestead Primer |
| `/evamod settle` | No | Founder's Homestead (once per overworld) |
| `/evamod town` | No | Find nearest town + safe landing coords |
| `/evamod town visit` | Yes | Teleport into a town house |
| `/evamod locate` | No | Find next house/town |
| `/evamod visit` | Yes | Teleport into house interior |
| `/evamod journal` / `mail` / `calendar` / `errand` / `near` | No | Homestead tools |
| `/evamod version` | No | Version + schema info |

## Building

Requires Java 25.

```
./gradlew build
```

Jar: `build/libs/evamod-2.1.0.jar`

## Running in dev

```
./gradlew runClient
```
