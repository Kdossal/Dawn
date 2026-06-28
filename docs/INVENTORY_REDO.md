# Inventory overlay redo

Full-screen inventory (`I` key) rebuild. Crafting lives in the world **C** menu — not in inventory tabs.

## Phase 1 — chrome only (done)

| Piece | Location |
|-------|----------|
| Layout | [`InventoryOverlayDesign`](core/src/main/java/com/dawn/ui/inventory/InventoryOverlayDesign.java) — 250×150 at 1×, × `INVENTORY_ART_MULT` (5), centered on HUD |
| Chrome | [`InventoryChrome`](core/src/main/java/com/dawn/ui/inventory/InventoryChrome.java) — `chrome_bg.png` only |
| Overlay | [`InventoryOverlay`](core/src/main/java/com/dawn/ui/inventory/InventoryOverlay.java) — dim layer, I toggle (Phase 1); slots added in Phase 2 |

**Removed (legacy):** `InventoryDesign` (200×148, `UI_SCALE=5`), tab stack, `ItemSlotWidget`, `InventoryGridPanel`, `InventoryCursorActor`, in-overlay `EquipmentTabPanel` / `StatCellWidget`, `CraftingTabPanel`. HUD drag uses a single [`InventoryCursorController`](core/src/main/java/com/dawn/ui/inventory/InventoryCursorController.java) via [`HudItemDragSession`](core/src/main/java/com/dawn/ui/HudItemDragSession.java).

## Phase 2 — grey slots (done)

| Piece | Location |
|-------|----------|
| Slot layout | [`InventoryOverlayDesign`](core/src/main/java/com/dawn/ui/inventory/InventoryOverlayDesign.java) — wear (123,15), accessories (123,40), off-hand (219,38), grid 5×3 from (123,71); 20px cells, 4px col gap, 3px row gap |
| Slots | [`InventoryOverlay`](core/src/main/java/com/dawn/ui/inventory/InventoryOverlay.java) — 4 wear + 4 accessory + off-hand + 15 grid `HudDragSlot`s |
| Drag | Shared [`HudItemDragSession`](core/src/main/java/com/dawn/ui/HudItemDragSession.java); cursor moves to overlay stage on open |

Tooltips deferred to Phase 3.

## Phase 3+ (planned)

1. **Red panel (left)** — character sprite, attributes, stats, status 6×2 grid
2. Tooltips on slot hover (`ItemTooltip` → `HudDragSlot`)

## Target architecture

1. **One slot widget** — `HudItemSlot` / `HudDragSlot` (same as hotbar, eqp sidebar, crate)
2. **One scale** — `INVENTORY_ART_MULT` (5) for overlay; gameplay HUD stays `HUD_ART_MULT` (3)
3. **One typography** — `DawnTypography` `TextContext.HUD`
4. **One drag cursor** — `HudDragCursorActor` on overlay stage; controller from `equipmentSidebar.dragSession()`

## Files preserved for Phase 2+

| File | Role |
|------|------|
| `InventoryCursorController` | Drag logic (HUD + future overlay slots) |
| `InventorySlotRef` | Slot identity |
| `ItemTooltip` | Hover hints (binds `Actor`) |
| `InventoryUiStyle` | Dim alpha, tooltip chrome |

## Crafting — world C-menu (done)

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

Crafting slots use **`HUD_ART_MULT`** — same scale path as hotbar/crate/inventory chrome.

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
Inventory chrome           → InventoryOverlayDesign 250×150 @ INVENTORY_ART_MULT (5); HUD slots @ HUD_ART_MULT (3)
```

## Do not regress

- Do not reintroduce `GameSettings.UiSize` / per-player HUD size toggles.
- Do not scale pause/debug button **layout** without matching typography (see `PauseUiStyle` fix).
- F3 debug uses `TextTier.SM` + `HUD_LAYOUT_SCALE = HUD_HEIGHT / 800` — batch HUD text, not Scene2D.

## Related docs

- [`HUD_SCALING.md`](HUD_SCALING.md) — completed HUD unification, test checklist, risk areas
