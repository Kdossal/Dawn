package com.dawn.ui.inventory;

import com.dawn.ui.DawnFonts;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.dawn.assets.DawnAssets;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;

/** Grid panel background {@link InventoryDesign#CONTENT_W}×{@link InventoryDesign#GRID_PANEL_H}; slots unchanged. */
public final class InventoryGridPanel extends Group {
    private final ItemSlotWidget[][] slots = new ItemSlotWidget[InventoryConstants.ROWS][InventoryConstants.COLS];
    private final Image[] rowHighlights = new Image[InventoryConstants.ROWS];
    private final float cellStep = InventoryDesign.SLOT_PX + InventoryDesign.GAP_PX;

    public InventoryGridPanel(DawnAssets assets, DawnFonts fonts, PlayerInventory inventory) {
        setSize(InventoryDesign.CONTENT_W, InventoryDesign.GRID_PANEL_H);

        Image gridBg = new Image(InventoryUiStyle.fixedDrawable(assets.uiInventory.gridPanel));
        gridBg.setSize(InventoryDesign.CONTENT_W, InventoryDesign.GRID_PANEL_H);
        gridBg.setPosition(0f, 0f);
        addActor(gridBg);

        for (int row = 0; row < InventoryConstants.ROWS; row++) {
            Image rowBg = new Image(InventoryUiStyle.fixedDrawable(assets.uiInventory.hotbarRow));
            rowBg.setSize(InventoryDesign.CONTENT_W, InventoryDesign.HOTBAR_ROW_H);
            float rowInset = (InventoryDesign.HOTBAR_ROW_H - InventoryDesign.SLOT_PX) / 2f;
            rowBg.setPosition(0f, rowY(row) - rowInset);
            rowBg.setVisible(false);
            rowHighlights[row] = rowBg;
            addActor(rowBg);
        }

        for (int row = 0; row < InventoryConstants.ROWS; row++) {
            for (int col = 0; col < InventoryConstants.COLS; col++) {
                int index = PlayerInventory.toIndex(row, col);
                ItemSlotWidget slot =
                        new ItemSlotWidget(
                                assets,
                                fonts,
                                InventorySlotRef.grid(index),
                                "",
                                assets.uiCommon.slot);
                slot.setPosition(slotX(col), rowY(row));
                slots[row][col] = slot;
                addActor(slot);
            }
        }

        refresh(inventory, assets);
    }

    private float slotX(int col) {
        return InventoryDesign.GRID_PAD + col * cellStep;
    }

    private float rowY(int row) {
        return InventoryDesign.GRID_PAD + (InventoryConstants.ROWS - 1 - row) * cellStep;
    }

    public void registerCursorController(InventoryCursorController controller) {
        for (int r = 0; r < InventoryConstants.ROWS; r++) {
            for (int c = 0; c < InventoryConstants.COLS; c++) {
                controller.registerSlot(slots[r][c]);
            }
        }
    }

    public ItemSlotWidget slotAt(int row, int col) {
        return slots[row][col];
    }

    public void refresh(PlayerInventory inventory, DawnAssets assets) {
        int activeRow = inventory.getActiveRow();
        for (int row = 0; row < InventoryConstants.ROWS; row++) {
            rowHighlights[row].setVisible(row == activeRow);
            for (int col = 0; col < InventoryConstants.COLS; col++) {
                slots[row][col].refresh(inventory.getSlot(row, col), assets);
            }
        }
    }
}
