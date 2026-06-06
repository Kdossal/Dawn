package com.dawn.ui.inventory;

import com.dawn.inventory.EquipmentSlot;

/** Identifies a drag/drop source or target slot. */
public final class InventorySlotRef {
    public enum Kind {
        GRID,
        EQUIPMENT
    }

    public final Kind kind;
    public final int gridIndex;
    public final EquipmentSlot equipmentSlot;

    private InventorySlotRef(Kind kind, int gridIndex, EquipmentSlot equipmentSlot) {
        this.kind = kind;
        this.gridIndex = gridIndex;
        this.equipmentSlot = equipmentSlot;
    }

    public static InventorySlotRef grid(int index) {
        return new InventorySlotRef(Kind.GRID, index, null);
    }

    public static InventorySlotRef equipment(EquipmentSlot slot) {
        return new InventorySlotRef(Kind.EQUIPMENT, -1, slot);
    }
}
