"""Generates the 64x64 player-format skins for every NPC biome variant,
plus stubby 64x64 plush pet skins.

Usage:  python scripts/generate_textures.py
Output: src/main/resources/assets/evamod/textures/entity/npc_<variant>.png
        src/main/resources/assets/evamod/textures/entity/pet_<name>.png
"""
import os
import random

from PIL import Image

OUT_DIR = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                       "assets", "evamod", "textures", "entity")

# skin, hair/headwear, shirt, pants, shoes
VARIANTS = {
    "plains":  ((233, 193, 158), (106, 76, 48),   (85, 128, 64),   (94, 74, 52),   (60, 45, 32)),
    "desert":  ((198, 150, 105), (240, 230, 200), (222, 196, 140), (176, 148, 98), (110, 86, 54)),
    "taiga":   ((226, 184, 148), (140, 70, 30),   (52, 84, 58),    (70, 70, 76),   (35, 30, 28)),
    "snowy":   ((232, 198, 168), (180, 190, 200), (70, 100, 140),  (40, 60, 90),   (150, 130, 100)),
    "savanna": ((130, 90, 60),   (25, 20, 18),    (200, 120, 50),  (120, 80, 40),  (80, 50, 30)),
    "jungle":  ((160, 110, 75),  (35, 28, 22),    (60, 140, 70),   (90, 110, 60),  (60, 70, 40)),
    "swamp":   ((190, 170, 130), (60, 80, 50),    (100, 90, 60),   (70, 80, 60),   (50, 50, 40)),
    "ocean":   ((210, 175, 145), (40, 90, 130),   (50, 140, 170),  (40, 70, 100),  (30, 50, 70)),
    "cherry":  ((240, 205, 185), (250, 170, 190), (255, 200, 220), (200, 140, 170), (160, 100, 130)),
    "badlands":((190, 130, 95),  (90, 50, 35),    (180, 90, 50),   (120, 70, 45),  (70, 40, 30)),
    "mushroom":((230, 200, 175), (200, 80, 90),   (180, 140, 170), (120, 90, 110), (90, 60, 70)),
    "dark":    ((170, 140, 110), (30, 25, 20),    (50, 55, 45),    (35, 40, 35),   (20, 18, 15)),
    "mountain":((220, 185, 155), (90, 95, 100),   (110, 120, 130), (70, 75, 80),   (50, 45, 40)),
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


def fill(img, rng, x1, y1, x2, y2, color):
    """Fills the inclusive rect with slightly noisy pixels of the color."""
    for x in range(x1, x2 + 1):
        for y in range(y1, y2 + 1):
            img.putpixel((x, y), jitter(color, rng) + (255,))


def draw_skin(name, colors):
    skin, hair, shirt, pants, shoes = colors
    rng = random.Random(name)  # deterministic per variant
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    # ---- Head (cube faces around (0,0)-(31,15)) ----
    fill(img, rng, 8, 0, 15, 7, hair)      # top: hair / headwear
    fill(img, rng, 16, 0, 23, 7, skin)     # bottom (chin)
    for (fx1, fx2) in ((0, 7), (8, 15), (16, 23), (24, 31)):  # right, front, left, back
        fill(img, rng, fx1, 8, fx2, 15, skin)
        fill(img, rng, fx1, 8, fx2, 10, hair)  # hair covers top 3 rows of each face
    # Face details on the front face (8,8)-(15,15)
    img.putpixel((10, 12), EYE_WHITE + (255,))
    img.putpixel((11, 12), EYE_PUPIL + (255,))
    img.putpixel((13, 12), EYE_PUPIL + (255,))
    img.putpixel((14, 12), EYE_WHITE + (255,))
    img.putpixel((11, 14), MOUTH + (255,))
    img.putpixel((12, 14), MOUTH + (255,))
    img.putpixel((13, 14), MOUTH + (255,))

    # ---- Body (20,16)-(39,31) ----
    fill(img, rng, 20, 16, 27, 19, shirt)  # top
    fill(img, rng, 28, 16, 35, 19, shirt)  # bottom
    fill(img, rng, 16, 20, 39, 31, shirt)  # right/front/left/back strip
    # Belt line
    fill(img, rng, 16, 30, 39, 30, shoes)

    # ---- Right arm (40,16)-(55,31), left arm (32,48)-(47,63) ----
    for (ox, oy) in ((40, 16), (32, 48)):
        fill(img, rng, ox + 4, oy, ox + 11, oy + 3, shirt)          # top + bottom
        fill(img, rng, ox, oy + 4, ox + 15, oy + 15, shirt)         # sides
        fill(img, rng, ox, oy + 12, ox + 15, oy + 15, skin)         # hands
        fill(img, rng, ox + 8, oy, ox + 11, oy + 3, skin)           # palm (bottom face)

    # ---- Right leg (0,16)-(15,31), left leg (16,48)-(31,63) ----
    for (ox, oy) in ((0, 16), (16, 48)):
        fill(img, rng, ox + 4, oy, ox + 11, oy + 3, pants)          # top + bottom
        fill(img, rng, ox, oy + 4, ox + 15, oy + 15, pants)         # sides
        fill(img, rng, ox, oy + 13, ox + 15, oy + 15, shoes)        # boots
        fill(img, rng, ox + 8, oy, ox + 11, oy + 3, shoes)          # soles

    out = os.path.join(OUT_DIR, f"npc_{name}.png")
    img.save(out)
    print(f"wrote {out}")


def generate_pet_texture(name, colors):
    """Draw a simpler stubby 64x64 plush-style skin."""
    body, accent, eyes, belly = colors
    rng = random.Random("pet_" + name)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    # Round plush head on classic head UV region
    fill(img, rng, 8, 0, 15, 7, body)
    fill(img, rng, 16, 0, 23, 7, belly)
    for (fx1, fx2) in ((0, 7), (8, 15), (16, 23), (24, 31)):
        fill(img, rng, fx1, 8, fx2, 15, body)
    # Soft belly patch on front face
    fill(img, rng, 10, 12, 13, 15, belly)
    # Big cute eyes
    img.putpixel((10, 11), EYE_WHITE + (255,))
    img.putpixel((11, 11), eyes + (255,))
    img.putpixel((13, 11), eyes + (255,))
    img.putpixel((14, 11), EYE_WHITE + (255,))
    # Tiny accent cheeks / ears tip
    fill(img, rng, 8, 8, 9, 9, accent)
    fill(img, rng, 14, 8, 15, 9, accent)
    img.putpixel((12, 14), accent + (255,))

    # Stubby body
    fill(img, rng, 20, 16, 27, 19, body)
    fill(img, rng, 28, 16, 35, 19, belly)
    fill(img, rng, 16, 20, 39, 31, body)
    fill(img, rng, 20, 24, 27, 30, belly)

    # Short chubby arms
    for (ox, oy) in ((40, 16), (32, 48)):
        fill(img, rng, ox + 4, oy, ox + 11, oy + 3, body)
        fill(img, rng, ox, oy + 4, ox + 15, oy + 12, body)
        fill(img, rng, ox, oy + 11, ox + 15, oy + 12, accent)

    # Stubby legs
    for (ox, oy) in ((0, 16), (16, 48)):
        fill(img, rng, ox + 4, oy, ox + 11, oy + 3, body)
        fill(img, rng, ox, oy + 4, ox + 15, oy + 12, body)
        fill(img, rng, ox, oy + 11, ox + 15, oy + 12, accent)

    out = os.path.join(OUT_DIR, f"pet_{name}.png")
    img.save(out)
    print(f"wrote {out}")


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name, colors in VARIANTS.items():
        draw_skin(name, colors)
    for name, colors in PETS.items():
        generate_pet_texture(name, colors)


if __name__ == "__main__":
    main()
