package com.dawn.inventory;

import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;

public final class EquipmentInventory {
    private final ItemStack[] slots = new ItemStack[EquipmentSlot.values().length];

    public EquipmentInventory() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = ItemStack.empty();
        }
    }

    public ItemStack get(EquipmentSlot slot) {
        return slots[slot.ordinal()].copy();
    }

    public void set(EquipmentSlot slot, ItemStack stack) {
        slots[slot.ordinal()] = stack == null || stack.isEmpty() ? ItemStack.empty() : stack.copy();
    }

    public ItemStack[] backingArray() {
        return slots;
    }

    public int indexOf(EquipmentSlot slot) {
        return slot.ordinal();
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot target) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ItemDef def = ItemRegistry.get(stack);
        return def != null && def.equipmentSlot() == target;
    }

    public boolean equip(EquipmentSlot slot, ItemStack incoming) {
        if (!canEquip(incoming, slot)) {
            return false;
        }
        set(slot, incoming);
        return true;
    }
}
