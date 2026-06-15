package com.dawn.entity.status;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.entity.StatFormulas;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.InventoryWeight;
import com.dawn.inventory.PlayerInventory;

public final class StatusSystem {
    private static final float BURDENED_FRACTION = 0.8f;
    private static final float HUNGRY_FRACTION = 0.5f;

    private StatusSystem() {}

    public static void refresh(Entity entity, PlayerInventory inventory, EquipmentInventory equipment) {
        if (entity == null || entity.getEntityId() != EntityId.PLAYER) {
            return;
        }
        StatusSet statuses = entity.getStatuses();
        statuses.clear();

        if (entity.isPoisoned()) {
            statuses.add(StatusId.POISONED);
        }

        float hunger = entity.getCurrentHunger();
        float maxHunger = entity.getMaxHunger();
        if (hunger <= 0f) {
            statuses.add(StatusId.STARVING);
        } else if (hunger <= maxHunger * HUNGRY_FRACTION) {
            statuses.add(StatusId.HUNGRY);
        }

        float carried = InventoryWeight.totalWeight(inventory, equipment);
        int maxWeight = StatFormulas.maxCarryWeight(entity.getStats());
        if (carried > maxWeight) {
            statuses.add(StatusId.IMMOBILE);
            statuses.add(StatusId.BURDENED);
        } else if (carried >= maxWeight * BURDENED_FRACTION) {
            statuses.add(StatusId.BURDENED);
        }
    }
}
