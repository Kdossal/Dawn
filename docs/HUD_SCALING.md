# HUD scaling and slot rendering

Summary of the 1920×1200 (`DISPLAY_SCALE = 3`) HUD work and where to verify behavior.

## Architecture

```
Constants.DISPLAY_SCALE = 3     → window / HUD coordinate space (1920×1200)
Constants.HUD_ART_MULT = 3      → hotbar, sidebar, crate slot art (1× PNG × 3)
Constants.VITALS_ART_MULT = 4   → vitals bars/icons only (slightly chunkier)
Click hints / status margins     → DISPLAY_SCALE / 2 (1.5×) for icons/margins
HUD typography (TextContext.HUD) → tier reference only (SM ≈ 32px screen, scale ×2)
F3 debug overlay                 → TextTier.SM + layout scaled HUD_HEIGHT/800 (see DebugOverlay)
Inventory overlay                → separate UI_SCALE = 5 (unchanged, looks small until redo)
```

## Canonical slot widget

| Component | Role |
|-----------|------|
| `HudSlotDesign` | Art-base layout (20× slot, 16× icon, gaps, hotbar row math) |
| `HudSlotChrome` | `HOTBAR` (slot/slotSelected), `DULL` (sidebar/crate), `FLOATING` (drag cursor, no bg) |
| `HudItemSlot` | bg + stretched icon + stack count |
| `HudDragSlot` | `HudItemSlot` + slot ref + drag hit target |
| `Hotbar` | Scene2D row of 10 `HudDragSlot` on shared equipment-sidebar `Stage` |

**Removed:** hotbar `SpriteBatch.render()`, `GameSettings.UiSize` / `slotMultiplier`, 10 invisible hotbar drag proxies.

## Manual test checklist

### Display and layout
- [ ] Window 1920×1200; world crisp at 3× nearest upscale
- [ ] Resize window: hotbar click and “over hotbar” interact suppression still correct (FitViewport letterboxing)

### Hotbar and slots
- [ ] Stack counts and icons match between hotbar, crate, equipment sidebar
- [ ] Selected hotbar slot uses `slotSelected` chrome
- [ ] Drag cursor shows icon + count only (no slot box)
- [ ] Hotbar 1–0, scroll, click selection; drag-lock when holding from grid slot

### Vitals and hints
- [ ] Vitals bars readable (4× art); text on hints/hotbar not oversized
- [ ] Status icons scale; F3 debug readable

### Pause menu
- [ ] Resume / Options / Exit / Back: text size good, button boxes ~320×52 (not 1.5× oversized)

### Inventory (known)
- [ ] `I` overlay opens; expected to look relatively small vs HUD until inventory redo

### Drag-drop
- [ ] Hotbar ↔ sidebar ↔ crate; break crate while open; world drop with LMB

## Risk areas (what could go wrong)

| Area | Risk | Where to look |
|------|------|----------------|
| Coordinate spaces | Screen vs HUD vs stage coords mixed | `GameScreen` hotbar click (unprojects), `EquipmentSidebarHud.isPointerOverHotbar` |
| Dual slot systems | HUD 3× vs inventory 5× drift | `ItemSlotWidget` vs `HudItemSlot` — intentional until inventory redo |
| Duplicate constants | Sidebar/crate inset differs from hotbar | `EquipmentSidebarDesign`, `CrateStorageDesign` vs `HudSlotDesign` |
| Vitals-only 4× | Bars out of proportion if `VITALS_ART_MULT` changed alone | `VitalsBarDesign`, `Constants.VITALS_ART_MULT` |
| Typography vs chrome | Pause/debug layout scaled without text | `PauseUiStyle`, `DebugOverlay` |
| Pixel align | Grid shimmer at 3× | `PixelAlign`, `DISPLAY_SCALE` |
| Placement tests | Old 1280×800 math | `WorldHudPopupPlacementTest`, `HudSlotDesignTest` |

## Files touched (reference)

**Core:** `Constants`, `GameSettings`, `DawnFonts`, `DawnTypography`, `PixelAlign`  
**HUD slots:** `HudSlotDesign`, `HudSlotChrome`, `HudItemSlot`, `HudDragSlot`, `Hotbar`, `HudDragCursorActor`, `HudItemDragSession`  
**HUD chrome:** `ClickHintRenderer`, `VitalsBarDesign`, `VitalsHud`, `StatusHud`, `PauseUiStyle`, `EquipmentSidebarHud`, `CrateStorageOverlay`  
**Game loop:** `ScreenRenderer`, `GameContext`, `GameScreen`, `PlayerAndInteractionPhase`  
**Not in scope:** `InventoryDesign`, `InventoryOverlay`, `ItemSlotWidget` — see [`INVENTORY_REDO.md`](INVENTORY_REDO.md)
