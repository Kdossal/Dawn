package com.dawn.gameplay.drops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.item.ItemId;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import org.junit.jupiter.api.Test;

class LootTableTest {
    private final LootTable loot = new LootTable();

    @Test
    void crateDropsCrateItem() {
        assertEquals(ItemId.CRATE, loot.roll(BlockId.CRATE, Layer.OBJECT).get(0).itemId);
    }

    @Test
    void sandGroundDropsSandItem() {
        assertEquals(ItemId.SAND, loot.roll(BlockId.SAND, Layer.GROUND).get(0).itemId);
    }

    @Test
    void rockBoulderDropsRockItem() {
        assertEquals(ItemId.ROCK, loot.roll(BlockId.ROCK, Layer.OBJECT).get(0).itemId);
    }

    @Test
    void bushDropsNothing() {
        assertTrue(loot.roll(BlockId.BUSH, Layer.OBJECT).isEmpty());
    }
}
