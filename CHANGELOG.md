# Changelog

## 3.0.0 — Companions

### Skins & folk
- **13 biome variants** (was 7): Ocean, Cherry, Badlands, Mushroom, Dark Forest, Mountain added
- **10 personalities** (was 6): Kind, Witty, Mysterious, Brave
- **14 jobs** (was 10): Baker, Beekeeper, Storyteller, Archaeologist
- Matching house palettes, loot tables, names, dialogue, and trades

### Stuffed-animal pets (cozy + helpful)
- Awaken an **Alive Plush** on the ground — 8 kinds (teddy, bunny, fox, cat, dragon, owl, frog, sheep)
- Follow / sit, ribbon dye cosmetics, treat reactions, speech bubbles
- Utilities: soft **glow**, **find settlement** hint, carry **one trinket**
- Immortal; never fights — `/evamod pet` status & controls

### Deep adventures
- **12 multi-step cozy stories** (`/evamod adventure`) with unlock gates and mail rewards
- Hooks into meeting NPCs, errands, pets, locate/town, journal, mail, calendar, gifts
- Landmark tracking for discovered towns

### Production
- Player schema **3**, network **3**, display **3.0.0 Companions**
- Saves from 2.x migrate forward via optional codec fields
- Primer book gains Pets + Adventures pages

## 2.1.0 — Homestead continuity

### Pre-mod / explored worlds
- **Homestead Primer** written book given once per player on join (new or existing worlds)
- `/evamod book` to obtain another copy without re-triggering once-logic
- `/evamod settle` plants a **Founder's Homestead** once per overworld (bounded cottage + NPC, no chunk regen)
- Login path is O(1): schema migrate + book flag + day-gated mail — **no structure scans**

### Forward compatibility
- `ModVersions` + player/world **schema versions**
- `DataMigrations` on join
- `EvaWorldData` world attachment
- `EvaContent` extension hooks for future packs

## 2.0.0 — Homestead (production base)

Town command, safe interior teleports, hearts, seasons, birthdays, gift tastes, errands, mail, journal.
Obsoletes 1.x.

## 1.1.5 and earlier — OUTDATED

Do not use. Known locate/teleport bugs.
