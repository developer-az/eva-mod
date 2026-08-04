"""Build cute front-facing NPC sprites from Eva Mod skin PNGs for the website."""
from __future__ import annotations

import os
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SKIN_DIR = ROOT / "src" / "main" / "resources" / "assets" / "evamod" / "textures" / "entity"
OUT_DIR = ROOT / "docs" / "assets" / "npc"

# Classic 64x64 front faces (x, y, w, h)
HEAD = (8, 8, 8, 8)
BODY = (20, 20, 8, 12)
ARM_R = (44, 20, 4, 12)
ARM_L = (36, 52, 4, 12)
LEG_R = (4, 20, 4, 12)
LEG_L = (20, 52, 4, 12)

SCALE = 10  # 16px wide body -> 160px; head 80px


def crop(skin: Image.Image, box: tuple[int, int, int, int]) -> Image.Image:
    x, y, w, h = box
    return skin.crop((x, y, x + w, y + h))


def build_sprite(skin_path: Path) -> Image.Image:
    skin = Image.open(skin_path).convert("RGBA")
    # Canvas: arms stick out 4px each side of 8px body => 16 wide, head+body+legs = 8+12+12 = 32
    canvas = Image.new("RGBA", (16, 32), (0, 0, 0, 0))
    head = crop(skin, HEAD)
    body = crop(skin, BODY)
    arm_r = crop(skin, ARM_R)
    arm_l = crop(skin, ARM_L)
    leg_r = crop(skin, LEG_R)
    leg_l = crop(skin, LEG_L)

    # Layout (Steve-style front):
    # head at (4,0), body (4,8), arms (0,8) and (12,8), legs (4,20) and (8,20)
    canvas.paste(head, (4, 0), head)
    canvas.paste(arm_r, (0, 8), arm_r)
    canvas.paste(body, (4, 8), body)
    canvas.paste(arm_l, (12, 8), arm_l)
    canvas.paste(leg_r, (4, 20), leg_r)
    canvas.paste(leg_l, (8, 20), leg_l)

    return canvas.resize((16 * SCALE, 32 * SCALE), Image.Resampling.NEAREST)


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for skin_path in sorted(SKIN_DIR.glob("npc_*.png")):
        name = skin_path.stem  # npc_plains
        sprite = build_sprite(skin_path)
        out = OUT_DIR / f"sprite_{name.removeprefix('npc_')}.png"
        sprite.save(out)
        print(f"wrote {out} ({sprite.size[0]}x{sprite.size[1]})")


if __name__ == "__main__":
    main()
