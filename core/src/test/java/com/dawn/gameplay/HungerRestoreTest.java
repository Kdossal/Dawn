package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.config.GameConfig;
import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import org.junit.jupiter.api.Test;

class HungerRestoreTest {
    @Test
    void overfill_halvesRemainderAboveMax() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.setHunger(80f);

        HungerRestore.apply(player, 50f);

        assertEquals(115f, player.getCurrentHunger(), 0.001f);
    }

    @Test
    void belowMax_noOverfillPenalty() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.setHunger(30f);

        HungerRestore.apply(player, 20f);

        assertEquals(50f, player.getCurrentHunger(), 0.001f);
    }
}
