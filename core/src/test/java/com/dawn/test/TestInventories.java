package com.dawn.test;

import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemStack;

public final class TestInventories {
    private TestInventories() {}

    public static PlayerInventory empty() {
        return emptyWithActiveRow(2);
    }

    public static PlayerInventory emptyWithActiveRow(int activeRow) {
        PlayerInventory inventory = new PlayerInventory();
        for (int i = 0; i < inventory.backingArray().length; i++) {
            inventory.setSlotAtIndex(i, ItemStack.empty());
        }
        inventory.setActiveRow(activeRow);
        return inventory;
    }
}
