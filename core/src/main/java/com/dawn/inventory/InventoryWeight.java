package com.dawn.inventory;

import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;

/** Carry weight from per-item weights × stack counts (inventory + equipment). */
public final class InventoryWeight {
    private InventoryWeight() {}

    public static float totalWeight(PlayerInventory inventory, EquipmentInventory equipment) {
        float total = 0f;
        if (inventory != null) {
            total += sumStacks(inventory.backingArray());
        }
        if (equipment != null) {
            total += sumStacks(equipment.backingArray());
        }
        return total;
    }

    private static float sumStacks(ItemStack[] slots) {
        float total = 0f;
        for (ItemStack stack : slots) {
            if (stack != null && !stack.isEmpty()) {
                ItemDef def = ItemRegistry.get(stack.itemId);
                if (def != null) {
                    total += stack.count * def.weightPerItem();
                }
            }
        }
        return total;
    }
}
