package com.dawn.item;

import com.dawn.inventory.EquipmentSlot;
import com.dawn.world.block.InteractionTag;

public record ItemDef(
        ItemId id,
        String displayName,
        String iconId,
        int maxStack,
        InteractionTag interactionTag,
        int reachCells,
        int toolPowerPercent,
        Placeable placeable,
        EquipmentSlot equipmentSlot) {

    public boolean isTool() {
        return interactionTag != null && interactionTag != InteractionTag.NONE;
    }

    public boolean canPlace() {
        return placeable != null;
    }

    public boolean canEquip() {
        return equipmentSlot != null;
    }

    public boolean matches(InteractionTag required) {
        if (required == null) {
            return false;
        }
        if (required == InteractionTag.NONE) {
            return true;
        }
        return interactionTag == required;
    }
}
