# Eva Mod — Companions (NeoForge 26.2)

Friendly, immortal biome NPCs with homes, friendship hearts, seasons, birthdays,
errands, mail, multi-house towns — plus **stuffed-animal pets** and **multi-step adventures**.

**Targets Minecraft 26.2 + NeoForge 26.2. Current release: 3.0.0 Companions.**

> **Outdated:** jars **1.0–1.1.x** are unsupported. Use **2.0.0+** (saves migrate to 3.0).

## What's new in 3.0

- **13 biome skins** (Ocean, Cherry, Badlands, Mushroom, Dark Forest, Mountain + originals)
- **8 living plush pets** — follow, sit, glow, find houses, carry a trinket (no combat)
- **12 adventure stories** — unlock as you explore (`/evamod adventure`)
- New jobs: Baker, Beekeeper, Storyteller, Archaeologist

## Already-explored worlds

Chunks generated **before** Eva Mod never received `npc_house` / `npc_town` pieces.
Natural generation still works in **new** chunks. For a world that was already explored:

1. Every player gets the **Homestead Primer** book once on join.
2. Run **`/evamod settle`** once per overworld for a Founder's Homestead.
3. Keep exploring outward for natural towns/houses.

Login never runs structure searches (MP-safe).

## Features

- Biome folk with personalities, jobs, gifts, trades, hearts, seasons, mail, journal
- Cozy stuffed pets (Alive Plush) with soft utilities
- Multi-step adventures tied to real gameplay (not combat)
- Homestead Primer with clickable commands
- Safe interior teleports; `/evamod town` / `locate` without cheats

## Commands

| Command | Cheats? | What it does |
|---------|---------|----------------|
| `/evamod book` | No | Homestead Primer |
| `/evamod settle` | No | Founder's Homestead (once) |
| `/evamod town` / `locate` | No | Find towns / houses |
| `/evamod visit` / `town visit` | Yes | Teleport inside |
| `/evamod journal` / `mail` / `calendar` / `errand` | No | Homestead tools |
| `/evamod pet` | No | Pet status, sit, follow, glow, find, here |
| `/evamod adventure` | No | List / start cozy adventure stories |
| `/evamod version` | No | Version + schema |

## Building

Requires Java 25.

```
./gradlew build
```

Jar: `build/libs/evamod-3.0.0.jar`

## Running in dev

```
./gradlew runClient
```

Regenerate NPC/pet textures:

```
pip install pillow
python scripts/generate_textures.py
```
