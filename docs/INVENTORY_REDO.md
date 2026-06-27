# Inventory overlay redo — notes for next pass

The full-screen inventory (`I` key) and crafting tab are **not** on the HUD scaling path. This doc captures what to preserve, replace, and watch when redoing inventory before/alongside crafting.

## Current state (intentionally legacy)

| Piece | Scale / coords | Notes |
|-------|----------------|-------|
| `InventoryDesign.UI_SCALE` | **5×** on 1× chrome (200×148 design px) | ~1000×740 screen px on 1920 window — looks small vs HUD |
| `ItemSlotWidget` | 16×16 design slots, `Scaling.fit` | Different icon scaling than HUD `stretch` |
| `InventoryCursorActor` | Manual `× UI_SCALE` font compensation | Hacky; floats in HUD stage space |
| `InventoryOverlay` | Own `FitViewport` + `inventoryRoot` scale | Isolated from equipment-sidebar `Stage` |
| `CraftingTabPanel` | Placeholder only | "Crafting — soon" |

**HUD path (done):** `HudSlotDesign` → `HudItemSlot` → hotbar / sidebar / crate @ `HUD_ART_MULT = 3`.

## Target architecture (recommended)

1. **Pick one slot widget** — extend `HudItemSlot` or share a common `ItemSlotView` used by both HUD and inventory, with chrome/style enum (like `HudSlotChrome`).
2. **Pick one scale strategy** — either:
   - **A)** Inventory chrome scales with `HUD_ART_MULT` (fills ~same proportion of 1920 window as other UI), or
   - **B)** Keep design-space layout but set `UI_SCALE = HUD_ART_MULT` (or derive from `Constants`) and re-layout chrome constants.
3. **One typography context** — inventory labels should use `TextContext.HUD` (or a renamed `SCREEN`) at tier SM, **not** `INVENTORY_DESIGN` fractional scales + manual `× UI_SCALE` on cursor.
4. **One drag cursor** — merge `InventoryCursorActor` and `HudDragCursorActor` patterns (`FLOATING` chrome, same count pad via `HudSlotDesign.countPadPx()` or equivalent).
5. **Shared drag controller** — `InventoryCursorController` already backs HUD drag; inventory overlay should register slots the same way as `HudDragSlot`.

## Files to touch in inventory redo

| File | Action |
|------|--------|
| `InventoryDesign.java` | Recompute `UI_SCALE`, slot px, or replace with `HudSlotDesign` multiples + panel insets |
| `InventoryOverlay.java` | Viewport/stage alignment with window; possibly same `HudViewport` |
| `ItemSlotWidget.java` | Replace with `HudItemSlot` wrapper or delegate |
| `InventoryCursorActor.java` | Use `HudItemSlot` FLOATING; drop manual font scale hack |
| `InventoryGridPanel.java`, `EquipmentTabPanel.java` | Slot sizing from shared design |
| `InventoryUiStyle.java`, `InventoryChrome.java` | Typography via `DawnTypography` HUD tiers |
| `CraftingTabPanel.java` | Real recipe UI (see below) |

## Crafting — world C-menu (in progress)

Crafting is **not** in the inventory tab (`CraftingTabPanel` stays a placeholder). Primary UI is a **popup above the player** toggled with **C**, mirroring [`CrateStorageOverlay`](../core/src/main/java/com/dawn/ui/CrateStorageOverlay.java) + [`WorldHudPopupPlacement`](../core/src/main/java/com/dawn/ui/WorldHudPopupPlacement.java).

### Step 0 (done)

| Piece | Location |
|-------|----------|
| Slot chrome | `crafting_slot.png` triplex → [`CraftingSlotDesign`](../core/src/main/java/com/dawn/ui/CraftingSlotDesign.java) |
| Panel layout | [`CraftingDesign`](../core/src/main/java/com/dawn/ui/CraftingDesign.java) — dynamic 1×3 … 4×2 grid, `panel.png` nine-patch |
| Recipe data | `com.dawn.gameplay.crafting.*` — [`RecipeRegistry`](../core/src/main/java/com/dawn/gameplay/crafting/RecipeRegistry.java), [`KnownRecipes`](../core/src/main/java/com/dawn/gameplay/crafting/KnownRecipes.java) on [`PlayerProfile`](../core/src/main/java/com/dawn/inventory/PlayerProfile.java) |
| Starter recipes | Hand context: campfire (2 logs, 3s, place), bandage (2 cloth, 3s, grab) |
| Removed items | `CRATE`, `BED`, `STONE_WALL`, `STONE_GROUND` no longer inventory items — craft-only structures |

### Phase 1 (done)

| Piece | Location |
|-------|----------|
| Overlay shell | [`CraftingOverlay`](../core/src/main/java/com/dawn/ui/CraftingOverlay.java) — C toggle, panel above player, dynamic slots |
| Slot display | [`CraftingSlotWidget`](../core/src/main/java/com/dawn/ui/CraftingSlotWidget.java) — icon only; clicks no-op until Phase 2 |
| C key | [`InputController.craftPressed()`](../core/src/main/java/com/dawn/input/InputController.java), [`PlayerAndInteractionPhase`](../core/src/main/java/com/dawn/game/PlayerAndInteractionPhase.java) |
| Hints | [`ClickHintRenderer`](../core/src/main/java/com/dawn/ui/ClickHintRenderer.java) — **E > C > I** (C always when HUD visible) |
| Mutual exclusion | C opens → closes crate; E opens crate → closes crafting; pause closes crafting |

### Phase 2 (done)

| Piece | Location |
|-------|----------|
| Affordability | [`CraftingAffordability`](core/src/main/java/com/dawn/gameplay/crafting/CraftingAffordability.java) — grid count, canAfford, consumeCosts |
| Execution | [`CraftingSystem`](core/src/main/java/com/dawn/gameplay/crafting/CraftingSystem.java) — grab channel, placement mode, placement channel |
| Slot clicks | [`CraftingSlotWidget`](core/src/main/java/com/dawn/ui/CraftingSlotWidget.java) → `CraftingSystem.onSlotClicked` |
| HUD cursor | [`InventoryCursorController.receiveCraftedStack`](core/src/main/java/com/dawn/ui/inventory/InventoryCursorController.java) — craft-origin cursor; drop on overlay close |
| Campfire preview | [`ItemRegistry`](core/src/main/java/com/dawn/item/ItemRegistry.java) — `CAMPFIRE` registered as placeable for phantom held / ghosts |
| Game loop | [`PlayerAndInteractionPhase`](core/src/main/java/com/dawn/game/PlayerAndInteractionPhase.java) — immobile grab channel, phantom held, craft placement tick |

### Phase 3 (done)

| Piece | Location |
|-------|----------|
| Slot overlays | [`CraftingSlotWidget`](core/src/main/java/com/dawn/ui/CraftingSlotWidget.java) — unavailable + selected on top of icon; time wipe (4th `crafting_slot` region, 80×20 sheet) |
| Overlay refresh | [`CraftingOverlay.refreshSlotChrome`](core/src/main/java/com/dawn/ui/CraftingOverlay.java) — afford, selected (grab + place), per-frame channel progress |
| RMB craft hint | [`ClickVerb.CRAFT`](core/src/main/java/com/dawn/gameplay/ClickVerb.java) — valid hover during placement mode |
| Facing | Grab channel: locked facing; place channel: face build target ([`PlayerAndInteractionPhase`](core/src/main/java/com/dawn/game/PlayerAndInteractionPhase.java)) |

### Phase 4 (done)

| Recipe | Context | Flow | Cost | Output |
|--------|---------|------|------|--------|
| Dirt ground | `SHOVEL` | Place | 1 dirt | `DIRT_GROUND` in pit |
| Sand ground | `SHOVEL` | Place | 1 sand | `SAND_GROUND` in pit |
| Stone ground | `SHOVEL` | Place | 2 rock | `STONE_GROUND` in pit |
| Lumber | `SAW` | Grab | 1 log | 2 lumber on HUD cursor |

| Piece | Location |
|-------|----------|
| Recipe data | [`RecipeRegistry`](core/src/main/java/com/dawn/gameplay/crafting/RecipeRegistry.java) — shovel place ×3, saw grab lumber |
| Craft-only items | `DIRT_GROUND`, `SAND_GROUND`, `STONE_GROUND` items (icons `dirt_ground`, `sand_ground`, `stone_ground`) — craft phantom / placement only; costs are `DIRT` / `SAND` / `ROCK` ingredients |
| Ground targets | Pits only — [`SurfaceRules.canPlaceGround`](core/src/main/java/com/dawn/world/block/SurfaceRules.java) |

Hammer context still has no recipes (future pass).

### Phase 5+ (planned)

1. Ingredient tooltips on slot hover

### Consumption rules

- **Grab** (bandage): materials consumed after craft timer; item on HUD grab cursor
- **Place** (campfire): materials consumed on successful world placement after timer
- Cancel before consume: no cost (placement mode does not pre-reserve)

Crafting slots use **`HUD_ART_MULT`** — same scale path as hotbar/crate, not `InventoryDesign.UI_SCALE`.

## Coordinate spaces checklist

When changing inventory, verify:

- [ ] Mouse → stage → slot hit tests (same as `EquipmentSidebarHud.isPointerOverInteractiveHud`)
- [ ] Tooltip anchor positions
- [ ] Cursor draw order (above slots, below nothing)
- [ ] Opening inventory closes crate (`UiModePhase` already closes crate on `I`)
- [ ] Hotbar drag session disabled while inventory open (`EquipmentSidebarHud.setInventoryOverlayOpen`)
- [ ] Stack counts match hotbar after craft/deposit

## Constants cheat sheet

```
DISPLAY_SCALE = 3          → 1920×1200 window
HUD_ART_MULT = 3           → hotbar / sidebar / crate slots (20×16 art-base)
VITALS_ART_MULT = 4        → vitals only
HUD typography             → DawnTypography TextContext.HUD, SM tier ≈ 32px
Inventory (today)          → UI_SCALE = 5 on 16px design slots — replace in redo
```

## Do not regress

- Do not reintroduce `GameSettings.UiSize` / per-player HUD size toggles.
- Do not scale pause/debug button **layout** without matching typography (see `PauseUiStyle` fix).
- F3 debug uses `TextTier.SM` + `HUD_LAYOUT_SCALE = HUD_HEIGHT / 800` — batch HUD text, not Scene2D.

## Related docs

- [`HUD_SCALING.md`](HUD_SCALING.md) — completed HUD unification, test checklist, risk areas
