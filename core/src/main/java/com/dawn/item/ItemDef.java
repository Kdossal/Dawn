package com.dawn.item;

import com.dawn.inventory.EquipmentSlot;
import com.dawn.world.block.InteractionTag;
import java.util.Set;

public record ItemDef(
        ItemId id,
        String displayName,
        String iconId,
        int maxStack,
        float weightPerItem,
        float eatHungerRestore,
        InteractionTag interactionTag,
        int reachCells,
        float weaponDamage,
        Placeable placeable,
        EquipmentSlot equipmentSlot) {

    public boolean isTool() {
        return interactionTag != null && interactionTag != InteractionTag.NONE;
    }

    public boolean canPlace() {
        return placeable != null;
    }

    public boolean isEdible() {
        return eatHungerRestore > 0f;
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

    /** True when this tool matches any non-NONE tag in {@code requiredTags}. */
    public boolean matchesAny(Set<InteractionTag> requiredTags) {
        if (requiredTags == null || requiredTags.isEmpty()) {
            return false;
        }
        for (InteractionTag tag : requiredTags) {
            if (tag != InteractionTag.NONE && matches(tag)) {
                return true;
            }
        }
        return false;
    }
}
