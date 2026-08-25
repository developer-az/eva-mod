"""Generates 64x64 player-format skins for NPC biome×gender variants
and distinctive plush pet skins (silhouette cues on hat/jacket overlays).

Design notes (Companions 3.1 visual identity)
--------------------------------------------
NPC: variant × gender — not random texture swaps.
  Color language stays biome-coded (desert sands, snowy blues, cherry pinks…).
  Gendered presentation via silhouette: hair length/volume, tunic hem vs belt+pants,
  and small accessories (scarf, hood, bandana) that still read as folk fantasy.
Pet: each PetKind gets unique ear/snout/horn/wing overlay shapes + body palette
  so scaled-down humanoid plushies no longer look identical.

Usage:  python scripts/generate_textures.py
"""
import os
import random

from PIL import Image

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                       "assets", "evamod", "textures", "entity")

# biome → (skin, hair, shirt, pants, shoes, accent)
VARIANTS = {
    "plains":   ((233, 193, 158), (106, 76, 48),   (85, 128, 64),   (94, 74, 52),   (60, 45, 32),   (200, 170, 80)),
    "desert":   ((198, 150, 105), (240, 230, 200), (222, 196, 140), (176, 148, 98), (110, 86, 54),  (180, 90, 50)),
    "taiga":    ((226, 184, 148), (140, 70, 30),   (52, 84, 58),    (70, 70, 76),   (35, 30, 28),   (160, 100, 60)),
    "snowy":    ((232, 198, 168), (180, 190, 200), (70, 100, 140),  (40, 60, 90),   (150, 130, 100),(220, 230, 240)),
    "savanna":  ((130, 90, 60),   (25, 20, 18),    (200, 120, 50),  (120, 80, 40),  (80, 50, 30),   (230, 180, 70)),
    "jungle":   ((160, 110, 75),  (35, 28, 22),    (60, 140, 70),   (90, 110, 60),  (60, 70, 40),   (220, 60, 70)),
    "swamp":    ((190, 170, 130), (60, 80, 50),    (100, 90, 60),   (70, 80, 60),   (50, 50, 40),   (120, 150, 80)),
    "ocean":    ((210, 175, 145), (40, 90, 130),   (50, 140, 170),  (40, 70, 100),  (30, 50, 70),   (240, 200, 100)),
    "cherry":   ((240, 205, 185), (250, 170, 190), (255, 200, 220), (200, 140, 170),(160, 100, 130),(255, 140, 170)),
    "badlands": ((190, 130, 95),  (90, 50, 35),    (180, 90, 50),   (120, 70, 45),  (70, 40, 30),   (210, 140, 70)),
    "mushroom": ((230, 200, 175), (200, 80, 90),   (180, 140, 170), (120, 90, 110), (90, 60, 70),   (255, 100, 120)),
    "dark":     ((170, 140, 110), (30, 25, 20),    (50, 55, 45),    (35, 40, 35),   (20, 18, 15),   (90, 120, 70)),
    "mountain": ((220, 185, 155), (90, 95, 100),   (110, 120, 130), (70, 75, 80),   (50, 45, 40),   (180, 190, 200)),
}

# body, accent, eyes, belly — cute stubby plush colors
PETS = {
    "teddy":  ((160, 110, 70),  (120, 80, 50),   (50, 40, 30),   (200, 160, 120)),
    "bunny":  ((240, 230, 220), (255, 180, 190), (60, 50, 50),   (255, 245, 240)),
    "fox":    ((220, 120, 50),  (245, 240, 230), (40, 30, 30),   (250, 220, 180)),
    "cat":    ((230, 180, 100), (250, 220, 160), (50, 120, 60),  (245, 230, 200)),
    "dragon": ((80, 160, 100),  (220, 80, 70),   (30, 30, 40),   (140, 200, 150)),
    "owl":    ((140, 100, 60),  (240, 230, 200), (220, 180, 40), (200, 170, 130)),
    "frog":   ((90, 170, 70),   (250, 230, 80),  (30, 40, 30),   (160, 210, 120)),
    "sheep":  ((245, 245, 245), (255, 180, 200), (40, 40, 40),   (230, 230, 235)),
}

EYE_WHITE = (245, 245, 245)
EYE_PUPIL = (60, 70, 120)
MOUTH = (170, 110, 90)


def jitter(rgb, rng, amount=8):
    return tuple(max(0, min(255, c + rng.randint(-amount, amount))) for c in rgb)


def fill(img, rng, x1, y1, x2, y2, color, amount=8):
    for x in range(x1, x2 + 1):
        for y in range(y1, y2 + 1):
            if 0 <= x < 64 and 0 <= y < 64:
                img.putpixel((x, y), jitter(color, rng, amount) + (255,))


def put(img, x, y, color):
    if 0 <= x < 64 and 0 <= y < 64:
        img.putpixel((x, y), color + (255,) if len(color) == 3 else color)


def draw_face(img, skin, hair, gender, accent, biome, rng):
    """Head UVs: top (8,0)-(15,7), bottom (16,0)-(23,7), faces row y=8..15."""
    fill(img, rng, 8, 0, 15, 7, hair)       # top
    fill(img, rng, 16, 0, 23, 7, skin)      # chin/bottom
    for fx1, fx2 in ((0, 7), (8, 15), (16, 23), (24, 31)):
        fill(img, rng, fx1, 8, fx2, 15, skin)

    if gender == "f":
        # Longer hair: covers more of sides + cascades on back/sides
        for fx1, fx2 in ((0, 7), (8, 15), (16, 23), (24, 31)):
            fill(img, rng, fx1, 8, fx2, 12, hair)
        fill(img, rng, 0, 11, 2, 15, hair)
        fill(img, rng, 29, 11, 31, 15, hair)
        fill(img, rng, 24, 10, 31, 15, hair)  # long back
        # Soft bangs fringe on front
        fill(img, rng, 8, 8, 15, 10, hair)
        put(img, 9, 11, hair)
        put(img, 14, 11, hair)
    else:
        # Short crop / tidy fringe
        for fx1, fx2 in ((0, 7), (8, 15), (16, 23), (24, 31)):
            fill(img, rng, fx1, 8, fx2, 10, hair)
        fill(img, rng, 8, 8, 15, 9, hair)

    # Biome accessories (hat overlay region 32..63, y 0..15 for second head layer)
    # We also paint base-layer accents so they show without slim overlays required.
    if biome == "desert":
        fill(img, rng, 8, 8, 15, 9, accent)  # headscarf band
        fill(img, rng, 32, 8, 39, 10, accent)
    elif biome == "snowy":
        fill(img, rng, 8, 0, 15, 7, accent)  # hood top
        fill(img, rng, 0, 8, 2, 14, accent)
        fill(img, rng, 29, 8, 31, 14, accent)
    elif biome == "ocean":
        fill(img, rng, 8, 8, 15, 9, accent)  # bandana
    elif biome == "cherry":
        put(img, 9, 9, accent)
        put(img, 14, 9, accent)
        put(img, 11, 8, accent)
    elif biome == "mushroom":
        fill(img, rng, 8, 0, 15, 7, accent)
        put(img, 10, 2, (255, 220, 220))
        put(img, 13, 4, (255, 220, 220))
    elif biome == "mountain":
        fill(img, rng, 8, 0, 15, 3, accent)  # knit cap brim
    elif biome == "jungle":
        put(img, 10, 8, accent)
        put(img, 13, 8, accent)
    elif biome == "badlands":
        fill(img, rng, 8, 8, 15, 9, (60, 40, 30))  # hat brim shadow

    # Eyes + mouth on front face (8,8)-(15,15)
    put(img, 10, 12, EYE_WHITE)
    put(img, 11, 12, EYE_PUPIL)
    put(img, 13, 12, EYE_PUPIL)
    put(img, 14, 12, EYE_WHITE)
    if gender == "f":
        put(img, 9, 13, accent)   # soft cheek tint
        put(img, 14, 13, accent)
    put(img, 11, 14, MOUTH)
    put(img, 12, 14, MOUTH)
    put(img, 13, 14, MOUTH)


def draw_body(img, skin, shirt, pants, shoes, accent, gender, biome, rng):
    # Body (20,16)-(39,31)
    fill(img, rng, 20, 16, 27, 19, shirt)
    fill(img, rng, 28, 16, 35, 19, shirt)
    fill(img, rng, 16, 20, 39, 31, shirt)

    if gender == "f":
        # Tunic / skirt hem — darker band lower torso + slight flare color
        fill(img, rng, 16, 27, 39, 31, pants)
        fill(img, rng, 20, 26, 27, 26, accent)
        # Soft collar
        fill(img, rng, 20, 20, 27, 21, accent)
    else:
        # Belt + clearer pants break
        fill(img, rng, 16, 29, 39, 31, pants)
        fill(img, rng, 16, 28, 39, 28, shoes)
        fill(img, rng, 20, 20, 27, 21, accent)  # suspenders / yoke

    # Biome shirt motif
    if biome in ("ocean", "snowy"):
        fill(img, rng, 22, 22, 25, 24, accent)
    elif biome == "plains":
        put(img, 23, 23, accent)
        put(img, 24, 24, accent)
    elif biome == "savanna":
        fill(img, rng, 21, 22, 26, 23, accent)

    # Arms
    for ox, oy in ((40, 16), (32, 48)):
        sleeve = shirt if gender == "m" else accent
        fill(img, rng, ox + 4, oy, ox + 11, oy + 3, sleeve)
        fill(img, rng, ox, oy + 4, ox + 15, oy + 15, sleeve)
        if gender == "f":
            # Slightly longer sleeves
            fill(img, rng, ox, oy + 11, ox + 15, oy + 13, shirt)
        fill(img, rng, ox, oy + 12, ox + 15, oy + 15, skin)
        fill(img, rng, ox + 8, oy, ox + 11, oy + 3, skin)

    # Legs
    for ox, oy in ((0, 16), (16, 48)):
        leg = pants
        fill(img, rng, ox + 4, oy, ox + 11, oy + 3, leg)
        fill(img, rng, ox, oy + 4, ox + 15, oy + 15, leg)
        if gender == "f":
            # Boot-length shorter under skirt read
            fill(img, rng, ox, oy + 12, ox + 15, oy + 15, shoes)
        else:
            fill(img, rng, ox, oy + 13, ox + 15, oy + 15, shoes)
        fill(img, rng, ox + 8, oy, ox + 11, oy + 3, shoes)


def draw_skin(name, gender, colors):
    skin, hair, shirt, pants, shoes, accent = colors
    rng = random.Random(f"{name}_{gender}")
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw_face(img, skin, hair, gender, accent, name, rng)
    draw_body(img, skin, shirt, pants, shoes, accent, gender, name, rng)
    out = os.path.join(OUT_DIR, f"npc_{name}_{gender}.png")
    img.save(out)
    # Keep legacy single-file alias = masculine for older references
    if gender == "m":
        img.save(os.path.join(OUT_DIR, f"npc_{name}.png"))
    print(f"wrote {out}")


def paint_overlay_rect(img, rng, x1, y1, x2, y2, color):
    fill(img, rng, x1, y1, x2, y2, color, amount=5)


def generate_pet_texture(name, colors):
    """Stubby plush with kind-specific silhouette cues on head/body overlays."""
    body, accent, eyes, belly = colors
    rng = random.Random("pet_" + name)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    fill(img, rng, 8, 0, 15, 7, body)
    fill(img, rng, 16, 0, 23, 7, belly)
    for fx1, fx2 in ((0, 7), (8, 15), (16, 23), (24, 31)):
        fill(img, rng, fx1, 8, fx2, 15, body)
    fill(img, rng, 10, 12, 13, 15, belly)

    # Kind-specific head silhouette (also hat layer 32–63 for extra volume)
    if name == "bunny":
        # Tall ears on top + hat layer
        fill(img, rng, 9, 0, 10, 6, body)
        fill(img, rng, 13, 0, 14, 6, body)
        fill(img, rng, 9, 1, 10, 5, accent)
        fill(img, rng, 13, 1, 14, 5, accent)
        paint_overlay_rect(img, rng, 40, 0, 41, 7, body)
        paint_overlay_rect(img, rng, 44, 0, 45, 7, body)
    elif name == "fox":
        # Pointed ears + white muzzle
        fill(img, rng, 8, 0, 9, 3, body)
        fill(img, rng, 14, 0, 15, 3, body)
        fill(img, rng, 10, 13, 13, 15, accent)
        put(img, 11, 14, (40, 30, 30))
        put(img, 12, 14, (40, 30, 30))
    elif name == "cat":
        fill(img, rng, 8, 0, 9, 4, body)
        fill(img, rng, 14, 0, 15, 4, body)
        put(img, 8, 1, accent)
        put(img, 15, 1, accent)
        # Whisker dots
        put(img, 9, 14, (80, 70, 60))
        put(img, 14, 14, (80, 70, 60))
    elif name == "dragon":
        # Horns + belly scales hint
        fill(img, rng, 9, 0, 10, 3, accent)
        fill(img, rng, 13, 0, 14, 3, accent)
        put(img, 10, 0, (240, 220, 100))
        put(img, 13, 0, (240, 220, 100))
        fill(img, rng, 11, 13, 12, 15, (60, 40, 40))  # snout
        paint_overlay_rect(img, rng, 40, 8, 42, 12, accent)  # tiny wing nubs
        paint_overlay_rect(img, rng, 45, 8, 47, 12, accent)
    elif name == "owl":
        # Big round face disk + ear tufts
        fill(img, rng, 9, 9, 14, 14, accent)
        fill(img, rng, 8, 0, 9, 2, body)
        fill(img, rng, 14, 0, 15, 2, body)
        put(img, 11, 14, (80, 50, 30))  # beak
        put(img, 12, 14, (80, 50, 30))
    elif name == "frog":
        # Eye bumps on top of head
        fill(img, rng, 9, 0, 11, 3, body)
        fill(img, rng, 12, 0, 14, 3, body)
        put(img, 10, 1, eyes)
        put(img, 13, 1, eyes)
        fill(img, rng, 10, 13, 13, 15, belly)
    elif name == "sheep":
        # Woolly cloud top
        fill(img, rng, 7, 0, 16, 4, belly)
        fill(img, rng, 8, 8, 15, 10, belly)
        put(img, 11, 14, accent)  # ribbon nose
        put(img, 12, 14, accent)
    else:  # teddy
        # Round ears
        fill(img, rng, 7, 1, 9, 4, body)
        fill(img, rng, 14, 1, 16, 4, body)
        put(img, 8, 2, accent)
        put(img, 15, 2, accent)
        put(img, 12, 13, accent)  # nose

    # Eyes
    put(img, 10, 11, EYE_WHITE)
    put(img, 11, 11, eyes)
    put(img, 13, 11, eyes)
    put(img, 14, 11, EYE_WHITE)
    put(img, 12, 14, accent)

    # Stubby body
    fill(img, rng, 20, 16, 27, 19, body)
    fill(img, rng, 28, 16, 35, 19, belly)
    fill(img, rng, 16, 20, 39, 31, body)
    fill(img, rng, 20, 24, 27, 30, belly)
    # Ribbon / belly accent stripe
    fill(img, rng, 20, 22, 27, 23, accent)

    for ox, oy in ((40, 16), (32, 48)):
        fill(img, rng, ox + 4, oy, ox + 11, oy + 3, body)
        fill(img, rng, ox, oy + 4, ox + 15, oy + 12, body)
        fill(img, rng, ox, oy + 11, ox + 15, oy + 12, accent)

    for ox, oy in ((0, 16), (16, 48)):
        fill(img, rng, ox + 4, oy, ox + 11, oy + 3, body)
        fill(img, rng, ox, oy + 4, ox + 15, oy + 12, body)
        fill(img, rng, ox, oy + 11, ox + 15, oy + 12, accent)

    out = os.path.join(OUT_DIR, f"pet_{name}.png")
    img.save(out)
    print(f"wrote {out}")


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name, colors in VARIANTS.items():
        for gender in ("m", "f"):
            draw_skin(name, gender, colors)
    for name, colors in PETS.items():
        generate_pet_texture(name, colors)


if __name__ == "__main__":
    main()
