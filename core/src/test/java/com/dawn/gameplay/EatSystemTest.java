package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.test.GameConfigTestSupport;
import com.dawn.test.TestInventories;
import org.junit.jupiter.api.Test;

class EatSystemTest {
    @Test
    void channelEat_consumesOneAndSetsInteractPulse() {
        GameConfigTestSupport.withEatDuration(0.5f, () -> {
            Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
            player.setPoisoned(false);
            player.setHunger(80f);
            var inventory = TestInventories.empty();
            inventory.setSlot(2, 0, ItemStack.of(ItemId.CANNED_FOOD, 2));
            inventory.setSelectedCol(0);
            EatSystem eat = new EatSystem();
            ItemStack held = inventory.getHeld();

            eat.update(player, inventory, held, true, 0.5f);

            assertEquals(1, inventory.getHeld().count);
            assertTrue(eat.isInteracting());
        });
    }

    @Test
    void cannotEatAtMaxHunger() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.setHunger(100f);

        assertFalse(EatSystem.canEat(player, ItemStack.of(ItemId.CANNED_FOOD)));
    }

    @Test
    void cannotEatNonEdible() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        var inventory = TestInventories.empty();
        inventory.setSlot(2, 0, ItemStack.of(ItemId.PICKAXE));

        assertFalse(EatSystem.canEat(player, inventory.getHeld()));
    }
}
