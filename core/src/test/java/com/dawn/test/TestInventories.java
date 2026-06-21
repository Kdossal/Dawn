package com.dawn.test;

import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemStack;

public final class TestInventories {
    private TestInventories() {}

    public static PlayerInventory empty() {
        return emptyWithSelectedIndex(0);
    }

    public static PlayerInventory emptyWithSelectedIndex(int selectedIndex) {
        PlayerInventory inventory = new PlayerInventory();
        for (int i = 0; i < inventory.backingArray().length; i++) {
            inventory.setSlotAtIndex(i, ItemStack.empty());
        }
        inventory.setSelectedIndex(selectedIndex);
        return inventory;
    }
}
