# Dawn

Top-down sandbox prototype — Java + LibGDX.

## Run

```bat
.\gradlew.bat desktop:run
```

Requires Java 17+. Run from the repo root (Gradle sets the assets working directory).

## Resolution

- **Logical view:** 640×400 (40×25 cells × **16px** per cell).
- **Window:** 1280×800 (**2×** integer upscale via `FitViewport`; nearest-filtered textures).
- **Art:** Tiles and entities are drawn at **native PNG size** (tiles/items usually 16×16; player 16×24). World uses logical 16px/cell; HUD uses window pixels (1280×800).
- **Entities:** Position `(x, y)` is **feet bottom-center**; movement uses a per-creature footprint in cells (`moveWidthCells` × `moveHeightCells`); sprite hitbox follows PNG bounds above the feet.

## Placeholder art

```bat
py -3 -m pip install -r tools/requirements.txt
py -3 tools/generate_assets.py
```

See [tools/README.md](tools/README.md). Replace PNGs later; keep filenames in sync with `ItemRegistry` / `BlockTextureId`. The default generator run skips existing files and is safe after you add art. **Never use `--force` or `--only`** on hand-made PNGs — those flags overwrite existing files.

## Controls

| Input | Action |
|--------|--------|
| WASD / arrows | Move |
| Double-tap WASD / arrows (while holding) | Run (+50% speed) |
| **1**–**9**, **0** | Select hotbar slot (active row) |
| Scroll wheel | Cycle hotbar slot |
| **E** | Open / close inventory overlay (game keeps running) |
| Inventory tabs | Equipment / Crafting (crafting panel blank for now) |
| Page Up / Page Down | Switch inventory row (3 rows; works while inventory open) |
| Click hotbar slot | Select slot |
| Drag items in inventory | Move between grid and equipment slots |
| Drop item outside inventory panel | Drop stack at player feet |
| Left click (hold) | Mine tile (matching tool; bushes with bare hands) |
| Right click (hold) | Place held placeable (dirt, sand, crate, bed, oak / spruce sapling) |
| **Q** | Drop 1 from held stack |
| **Shift+Q** | Drop full held stack |
| Walk near drops | Pick up (player-dropped items have a short cooldown) |
| **F3** | Debug overlay (OFF → stats → stats + world hitboxes) |

Click the game window for keyboard focus.

## Project structure

```
core/src/main/java/com/dawn/
  DawnGame.java
  config/             Constants, GameConfig
  game/               GameScreen, GameContext, GameLoop, ScreenRenderer
  input/              InputController, GameCursor
  entity/             Entity, EntityMovementSolver, EntityRegistry
  item/               ItemId, ItemStack, ItemDef, ItemRegistry
  inventory/          PlayerInventory, EquipmentInventory, PlayerProfile (data)
  assets/             DawnAssets (+ UiCommon / UiInventory / UiEquipment)
  render/             RenderColors, BatchDraw, GameViewport
  ui/
    Hotbar.java, HudAssets.java, DebugOverlay.java
    inventory/        Overlay, chrome, grid, drag-drop, slots, layout
      tab/            Tab selectors + crafting placeholder
    equipment/        Equipment tab panel + stat cells
  world/              World (ground / floor / object layers), structures, WorldRenderer
  gameplay/           mining, placement/, drops, InteractionPresentation
desktop/              LWJGL launcher
tools/                generate_assets.py, assets_manifest.json
assets/
  tiles/ items/ entities/
  ui/common/          slot art (hotbar + inventory)
  ui/inventory/       chrome, grid, tabs
  ui/equipment/       stat cells, XP bar
```

## Tuning

[`GameConfig.java`](core/src/main/java/com/dawn/config/GameConfig.java) — movement, reach, pickup radius/cooldown, simulation.

[`RenderColors.java`](core/src/main/java/com/dawn/render/RenderColors.java) — highlight and HUD overlay alphas.

## Runtime Architecture

See [`docs/game-loop-phases.md`](docs/game-loop-phases.md) for the current `GameScreen` phase pipeline and behavior-preserving ordering contract.
