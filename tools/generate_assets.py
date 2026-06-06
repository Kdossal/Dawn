#!/usr/bin/env python3
"""
Generate rudimentary placeholder PNG assets for Dawn.

By default, existing PNGs are never overwritten (safe for hand-made art).
Use --force only when you intentionally want to regenerate placeholders.

Usage:
  pip install -r tools/requirements.txt
  python tools/generate_assets.py              # missing files only
  python tools/generate_assets.py --force      # overwrite all manifest PNGs

Outputs:
  assets/tiles/<name>.png       (16x16)
  assets/items/<name>.png       (16x16)
  assets/entities/<name>.png    (16x16)
  assets/ui/<group>/<name>.png  (common, inventory, equipment)
  assets/ASSETS.txt             (index)
"""

from __future__ import annotations

import argparse
import json
import random
import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("Install Pillow: pip install -r tools/requirements.txt", file=sys.stderr)
    sys.exit(1)

ROOT = Path(__file__).resolve().parent.parent
MANIFEST_PATH = Path(__file__).resolve().parent / "assets_manifest.json"
ASSETS_ROOT = ROOT / "assets"


def hex_to_rgba(hex_color: str, alpha: int = 255) -> tuple[int, int, int, int]:
    h = hex_color.lstrip("#")
    r, g, b = int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)
    return r, g, b, alpha


def darken(rgba: tuple[int, int, int, int], factor: float = 0.75) -> tuple[int, int, int, int]:
    r, g, b, a = rgba
    return int(r * factor), int(g * factor), int(b * factor), a


def draw_border(
    draw: ImageDraw.ImageDraw,
    width: int,
    height: int,
    color: tuple[int, int, int, int],
    border: int = 1,
) -> None:
    edge = darken(color, 0.55)
    for i in range(border):
        draw.rectangle([i, i, width - 1 - i, height - 1 - i], outline=edge)


def draw_noise(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int], seed: int = 0) -> None:
    rng = random.Random(seed)
    for y in range(size):
        for x in range(size):
            if rng.random() < 0.35:
                c = darken(base, 0.85 + rng.random() * 0.15)
                draw.point((x, y), fill=c)
            elif rng.random() < 0.12:
                c = darken(base, 1.05)
                draw.point((x, y), fill=c)


def draw_dots(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    for y in range(0, size, 4):
        for x in range(0, size, 4):
            draw.point((x + 1, y + 1), fill=darken(base, 0.7))


def draw_brick(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    line = darken(base, 0.5)
    for y in range(0, size, 4):
        off = 0 if (y // 4) % 2 == 0 else 4
        for x in range(-off, size, 8):
            draw.line([(x, y), (x + 8, y)], fill=line, width=1)
    for x in range(0, size, 8):
        draw.line([(x, 0), (x, size)], fill=line, width=1)


def draw_waves(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    accent = darken(base, 0.8)
    for i in range(3):
        y = 4 + i * 5
        draw.arc([0, y - 2, size, y + 4], 0, 180, fill=accent, width=1)


def draw_rock(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    cx, cy = size // 2, size // 2
    draw.ellipse([cx - 5, cy - 4, cx + 5, cy + 5], fill=darken(base, 0.9))
    draw.ellipse([cx - 3, cy - 5, cx + 4, cy + 2], fill=base)
    draw.line([(cx - 2, cy), (cx + 2, cy + 2)], fill=darken(base, 0.6), width=1)


def draw_wood(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    grain = darken(base, 0.65)
    draw.rectangle([5, 3, 10, size - 3], fill=base)
    for y in range(4, size - 2, 3):
        draw.line([(6, y), (9, y)], fill=grain, width=1)


def draw_bush(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    draw.ellipse([2, 6, 9, 13], fill=darken(base, 0.85))
    draw.ellipse([6, 4, 13, 11], fill=base)
    draw.ellipse([4, 8, 11, 14], fill=darken(base, 0.9))


def draw_pickaxe(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    handle = darken(base, 0.6)
    draw.line([(8, 12), (8, 5)], fill=handle, width=2)
    draw.line([(4, 5), (12, 5)], fill=base, width=2)
    draw.point((4, 5), fill=darken(base, 0.5))
    draw.point((12, 5), fill=darken(base, 0.5))


def draw_axe(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    draw.line([(8, 12), (8, 6)], fill=darken(base, 0.6), width=2)
    draw.pieslice([9, 3, 14, 10], 280, 80, fill=base)


def draw_shovel(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    draw.line([(8, 12), (8, 6)], fill=darken(base, 0.6), width=2)
    draw.rectangle([6, 3, 10, 6], fill=base)


def draw_player(draw: ImageDraw.ImageDraw, size: int, base: tuple[int, int, int, int]) -> None:
    # simple top-down body + head
    draw.ellipse([5, 2, 10, 7], fill=darken(base, 0.85))
    draw.rectangle([5, 7, 10, 13], fill=base)
    draw.rectangle([4, 9, 5, 12], fill=darken(base, 0.75))
    draw.rectangle([10, 9, 11, 12], fill=darken(base, 0.75))


def draw_slot(draw: ImageDraw.ImageDraw, width: int, height: int, base: tuple[int, int, int, int]) -> None:
    draw.rectangle([0, 0, width - 1, height - 1], fill=base)
    draw_border(draw, width, height, darken(base, 1.2), border=2)
    inner = darken(base, 0.9)
    m = min(width, height)
    inset = min(3, m // 4)
    draw.rectangle([inset, inset, width - 1 - inset, height - 1 - inset], outline=inner)


def pattern_solid(_d, _w, _h, _b) -> None:
    pass


def pattern_slot(d, w, h, b) -> None:
    draw_slot(d, w, h, b)


def pattern_square(fn):
    def wrapper(d, w, h, b) -> None:
        s = min(w, h)
        fn(d, s, b)

    return wrapper


PATTERNS = {
    "solid": pattern_solid,
    "noise": pattern_square(draw_noise),
    "dots": pattern_square(draw_dots),
    "brick": pattern_square(draw_brick),
    "waves": pattern_square(draw_waves),
    "rock": pattern_square(draw_rock),
    "wood": pattern_square(draw_wood),
    "bush": pattern_square(draw_bush),
    "pickaxe": pattern_square(draw_pickaxe),
    "axe": pattern_square(draw_axe),
    "shovel": pattern_square(draw_shovel),
    "player": pattern_square(draw_player),
    "slot": pattern_slot,
}


def try_load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for name in ("arial.ttf", "Arial.ttf", "segoeui.ttf"):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def asset_dimensions(spec: dict, default_size: int) -> tuple[int, int]:
    w = spec.get("width", spec.get("w", default_size))
    h = spec.get("height", spec.get("h", default_size))
    return int(w), int(h)


def render_asset(name: str, spec: dict, default_size: int) -> Image.Image:
    color = hex_to_rgba(spec.get("color", "#FF00FF"), spec.get("alpha", 255))
    pattern = spec.get("pattern", "solid")
    label = spec.get("label", "")
    width, height = asset_dimensions(spec, default_size)

    img = Image.new("RGBA", (width, height), color)
    draw = ImageDraw.Draw(img)
    draw_border(draw, width, height, color)

    fn = PATTERNS.get(pattern)
    if fn:
        fn(draw, width, height, color)

    if label:
        font = try_load_font(max(6, min(width, height) // 3))
        bbox = draw.textbbox((0, 0), label, font=font)
        tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
        tx = (width - tw) // 2
        ty = (height - th) // 2
        draw.text((tx + 1, ty + 1), label, fill=(0, 0, 0, 180), font=font)
        draw.text((tx, ty), label, fill=(255, 255, 255, 230), font=font)

    return img


def is_asset_spec(value: object) -> bool:
    return isinstance(value, dict) and "color" in value


def write_png(path: Path, img: Image.Image, force: bool) -> bool:
    """Save image when path is missing or force is set. Returns True if written."""
    if path.exists() and not force:
        print(f"  skip {path.relative_to(ROOT)} (already exists)")
        return False
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    print(f"  wrote {path.relative_to(ROOT)}")
    return True


def iter_ui_writes(ui_root: Path, ui_size: int, ui_manifest: dict) -> list[tuple[str, Path, int, dict]]:
    """Flatten nested ui manifest groups into (index_label, output_path, size, spec)."""
    writes: list[tuple[str, Path, int, dict]] = []
    for key, value in ui_manifest.items():
        if not is_asset_spec(value) and isinstance(value, dict):
            group_dir = ui_root / key
            for name, spec in value.items():
                writes.append((f"ui/{key}/{name}", group_dir / f"{name}.png", ui_size, spec))
    return writes


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate Dawn placeholder PNG assets.")
    parser.add_argument(
        "--force",
        action="store_true",
        help="Overwrite existing PNGs (default: only create missing files)",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    force = args.force
    manifest = json.loads(MANIFEST_PATH.read_text(encoding="utf-8"))
    tile_size = manifest.get("tileSize", 16)
    item_size = manifest.get("itemSize", 16)
    entity_size = manifest.get("entitySize", 16)
    ui_size = manifest.get("uiSize", 32)

    flat_categories = [
        ("tiles", ASSETS_ROOT / "tiles", tile_size, manifest.get("tiles", {})),
        ("items", ASSETS_ROOT / "items", item_size, manifest.get("items", {})),
        ("entities", ASSETS_ROOT / "entities", entity_size, manifest.get("entities", {})),
    ]

    wrote = 0
    skipped = 0
    index_lines = [
        "Dawn assets index.",
        "Generate missing placeholders: python tools/generate_assets.py",
        "Existing PNGs are never overwritten unless you pass --force.",
        "Hand-made art: drop PNGs here or edit tools/assets_manifest.json.",
        "",
    ]

    for cat, out_dir, size, entries in flat_categories:
        out_dir.mkdir(parents=True, exist_ok=True)
        index_lines.append(f"[{cat}/]")
        for name, spec in entries.items():
            path = out_dir / f"{name}.png"
            if write_png(path, render_asset(name, spec, size), force):
                wrote += 1
            else:
                skipped += 1
            if path.exists():
                index_lines.append(f"  {name}.png")
        index_lines.append("")

    ui_writes = iter_ui_writes(ASSETS_ROOT / "ui", ui_size, manifest.get("ui", {}))
    current_group = ""
    for label, path, size, spec in ui_writes:
        group = label.split("/")[1]
        if group != current_group:
            if current_group:
                index_lines.append("")
            index_lines.append(f"[ui/{group}/]")
            current_group = group
        if write_png(path, render_asset(path.stem, spec, size), force):
            wrote += 1
        else:
            skipped += 1
        if path.exists():
            index_lines.append(f"  {path.name}")

    index_path = ASSETS_ROOT / "ASSETS.txt"
    index_lines.append("")
    index_path.write_text("\n".join(index_lines), encoding="utf-8")
    print(f"\nWrote {wrote} file(s), skipped {skipped} existing under {ASSETS_ROOT.relative_to(ROOT)}/")
    if skipped and not force:
        print("Use --force to regenerate placeholders and overwrite hand-made PNGs.")
    print(f"Index: {index_path.relative_to(ROOT)}")


if __name__ == "__main__":
    print("Dawn asset generator\n")
    main()
