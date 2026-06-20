package com.dawn.entity.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.test.TestInventories;
import org.junit.jupiter.api.Test;

class StatusSystemTest {
    @Test
    void spawn_hasHungryAndPoisoned() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        PlayerInventory inventory = new PlayerInventory();

        StatusSystem.refresh(player, inventory, new EquipmentInventory());

        assertTrue(player.getStatuses().has(StatusId.HUNGRY));
        assertTrue(player.getStatuses().has(StatusId.POISONED));
        assertFalse(player.getStatuses().has(StatusId.STARVING));
    }

    @Test
    void zeroHunger_isStarving() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.setPoisoned(false);
        player.adjustVitals(0f, 0f, -player.getCurrentHunger(), 0f);

        StatusSystem.refresh(player, new PlayerInventory(), new EquipmentInventory());

        assertTrue(player.getStatuses().has(StatusId.STARVING));
        assertFalse(player.getStatuses().has(StatusId.HUNGRY));
    }

    @Test
    void underEightyPercentWeight_hasNoWeightStatuses() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.setPoisoned(false);
        player.adjustVitals(0f, 0f, player.getMaxHunger() - player.getCurrentHunger(), 0f);
        PlayerInventory inventory = new PlayerInventory();

        StatusSystem.refresh(player, inventory, new EquipmentInventory());

        assertFalse(player.getStatuses().has(StatusId.BURDENED));
        assertFalse(player.getStatuses().has(StatusId.IMMOBILE));
    }

    @Test
    void atEightyPercent_isBurdenedOnly() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        PlayerInventory inventory = TestInventories.empty();
        // max weight 20; 80% = 16. Eight tools at 2 each = 16.
        for (int i = 0; i < 8; i++) {
            inventory.setSlotAtIndex(i, ItemStack.of(ItemId.HAMMER));
        }

        StatusSystem.refresh(player, inventory, new EquipmentInventory());

        assertTrue(player.getStatuses().has(StatusId.BURDENED));
        assertFalse(player.getStatuses().has(StatusId.IMMOBILE));
    }

    @Test
    void overMaxWeight_isBurdenedAndImmobile() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        PlayerInventory inventory = TestInventories.empty();
        // Eleven tools at 2 each = 22 (> 20).
        for (int i = 0; i < 11; i++) {
            inventory.setSlotAtIndex(i, ItemStack.of(ItemId.HAMMER));
        }

        StatusSystem.refresh(player, inventory, new EquipmentInventory());

        assertTrue(player.getStatuses().has(StatusId.BURDENED));
        assertTrue(player.getStatuses().has(StatusId.IMMOBILE));
    }

    @Test
    void burdened_halvesMoveSpeed() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.getStatuses().add(StatusId.BURDENED);

        assertEquals(2.5f, player.getMoveSpeedCellsPerSec(false), 0.001f);
    }

    @Test
    void immobile_zeroMoveSpeed() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.getStatuses().add(StatusId.IMMOBILE);

        assertEquals(0f, player.getMoveSpeedCellsPerSec(false), 0.001f);
    }
}
