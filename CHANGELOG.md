# Changelog

## 3.1.0 — Companions identity & home warmth

### Visual identity
- NPC skins are now **variant × gender** (hair/tunic silhouettes + biome accessories), not one shared look
- Gendered name pools match presentation
- Pets get distinct plush silhouettes, scales, and soft per-kind sounds

### Pets do things
- Right-click opens a **pet menu** (sit/follow, glow, find town, cheer, trinket tip) mirroring NPC dialogue UX

### Protective neighbors
- NPCs **fend off hostiles near home** with knockback, a light tap, and brief Weakness (cozy, not OP)

### Deeper dialogue
- Ask Day, Compliment, Tour, Story actions added alongside Talk / Trade / Help

### Bed fix
- Beds only place when both halves fit the chunk box (stops half-bed item pops)
- Sleep AI never treats home as a bed; properly sets/clears OCCUPIED

### Production
- Display **3.1.0**, network **4** (pet menu payloads). Entity gender NBT is additive for old saves.

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
