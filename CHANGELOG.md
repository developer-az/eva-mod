# Changelog

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
