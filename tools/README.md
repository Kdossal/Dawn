# Dawn asset tools

## Hand-made art — read this first

**Do not run the generator against PNGs you have painted or edited by hand.**

| Command | Safe with hand-made art? |
|---------|--------------------------|
| `py -3 tools/generate_assets.py` | **Yes** — creates **missing** files only; skips existing PNGs |
| `py -3 tools/generate_assets.py --only …` | **No** — **overwrites** those files even if they already exist |
| `py -3 tools/generate_assets.py --force` | **No** — **overwrites every** manifest PNG |

Once you replace a placeholder with real pixel art, **never** pass `--force` or `--only`. Edit PNGs directly in `assets/`, or copy files in from elsewhere. Commit art to git so you can recover it.

The script also rewrites `assets/ASSETS.txt` when it runs. If you maintain that file by hand, back it up first or restore it from git after a generator run.

## Generate placeholder art

Rudimentary PNGs aligned with game ids. Swap files later without renaming (keep exact pixel dimensions for inventory chrome art).

**Default run is safe:** existing PNGs are skipped. Use `--force` or `--only` only when you deliberately want to throw away current files and regenerate placeholders from scratch.

```bat
py -3 -m pip install -r tools/requirements.txt
py -3 tools/generate_assets.py
```

**Destructive (do not use on hand-made art):**

```bat
py -3 tools/generate_assets.py --force
py -3 tools/generate_assets.py --only pit water
```

(Linux/macOS: use `python3` instead of `py -3`.)

UI font: [`assets/fonts/m5x7.ttf`](../assets/fonts/m5x7.ttf) is loaded at runtime via LibGDX FreeType (see `DawnFonts`). Smoke-test with `./gradlew fontPreview`.

## Output

| Folder | Contents |
|--------|----------|
| `assets/tiles/` | One PNG per block texture |
| `assets/items/` | One PNG per item `iconId` from `ItemRegistry` |
| `assets/entities/` | `base.png` |
| `assets/ui/common/` | Shared 16×16 slots (`slot`, `slot_selected`, `slot_equip`) |
| `assets/ui/inventory/` | Inventory chrome, grid, tabs, hotbar row highlight |
| `assets/ui/equipment/` | Equipment tab stat cells and XP bar |

### Inventory UI art (1× design pixels)

Rendered at **`HUD_ART_MULT`** (3×) on screen. Replace PNGs at these **exact** 1× sizes:

| Path | Size (W×H) |
|------|------------|
| `inventory/chrome_bg` | 250×150 |
| `inventory/tab_page` | 193×74 |
| `inventory/grid_panel` | 193×60 |
| `inventory/tab_equipment` / `tab_equipment_active` | 12×8 each |
| `inventory/tab_crafting` / `tab_crafting_active` | 12×8 each |
| `inventory/hotbar_row` | 193×20 |
| `equipment/stat_cell` | 55×22 |
| `common/slot`, `slot_selected`, `slot_equip` | 16×16 |
| `equipment/exp_bar_bg` / `exp_bar_fill` | 40×4 |

Manifest entries may set `"width"` and `"height"` per UI asset (see `generate_assets.py`).

The game loads **individual PNGs** via `DawnAssets` (`uiCommon`, `uiInventory`, `uiEquipment`), not atlas sheets.

## Block sprite layout (game)

World block **draw size, anchor, and offsets** live in [`core/src/main/resources/block_visuals.json`](../core/src/main/resources/block_visuals.json) (loaded at startup). When you change placeholder dimensions for tiles in [`assets_manifest.json`](assets_manifest.json), mirror `width`/`height` in `block_visuals.json` so culling, Y-sort, and occlusion stay correct. Field names use Java enums: `blockId` (`BlockId`), `textureId` (`BlockTextureId`), `anchor` (`VisualAnchor`).

## Adding new assets

1. **Block / tile sprites:** Add `BlockTextureId` and PNG under `assets/tiles/`; append an entry to `block_visuals.json`; run `generate_assets.py` only to fill **missing** placeholders.
2. Edit [`assets_manifest.json`](assets_manifest.json) for placeholders (tiles / ui groups as needed).
3. Run `generate_assets.py` to create any new manifest entries that do not have a PNG yet. **Do not** use `--force` or `--only` if you have hand-made PNGs in `assets/`.
4. Register items in Java (`ItemRegistry`, etc.) using the same **filename** (without `.png`) where applicable.
5. For new UI regions, add a field to the matching `DawnAssets.Ui*` class and wire the load path.

### Patterns

`solid`, `noise`, `dots`, `brick`, `waves`, `rock`, `wood`, `bush`, `pickaxe`, `axe`, `shovel`, `player`, `slot`

## Replacing with real pixel art

Replace any `assets/**/<name>.png` directly in an image editor or file explorer. **Do not** run `generate_assets.py --force` or `--only` on folders that contain art you care about — those flags overwrite existing PNGs.
