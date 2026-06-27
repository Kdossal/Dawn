package com.dawn.gameplay.drops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class LootTableTest {
    private final LootTable loot = new LootTable(new Random(1L));

    @Test
    void crateDropsLumberGuaranteed() {
        List<ItemStack> drops = loot.roll(BlockId.CRATE, Layer.OBJECT);
        assertEquals(ItemId.LUMBER, drops.get(0).itemId);
        assertEquals(1, drops.get(0).count);
    }

    @Test
    void sandGroundDropsSandItem() {
        assertEquals(ItemId.SAND, loot.roll(BlockId.SAND_GROUND, Layer.GROUND).get(0).itemId);
    }

    @Test
    void rockBoulderDropsRockItem() {
        assertEquals(ItemId.ROCK, loot.roll(BlockId.ROCK, Layer.OBJECT).get(0).itemId);
    }

    @Test
    void stoneGroundDropsTwoRocks() {
        ItemStack drop = loot.roll(BlockId.STONE_GROUND, Layer.GROUND).get(0);
        assertEquals(ItemId.ROCK, drop.itemId);
        assertEquals(2, drop.count);
    }

    @Test
    void dirtGroundDropsDirtItem() {
        assertEquals(ItemId.DIRT, loot.roll(BlockId.DIRT_GROUND, Layer.GROUND).get(0).itemId);
    }

    @Test
    void bushDropsNothing() {
        assertTrue(loot.roll(BlockId.BUSH, Layer.OBJECT).isEmpty());
    }

    @Test
    void campfireDropsNothing() {
        assertTrue(loot.roll(BlockId.CAMPFIRE, Layer.OBJECT).isEmpty());
    }

    @Test
    void stoneWallDropsTwoRocks() {
        ItemStack drop = loot.roll(BlockId.STONE_WALL, Layer.OBJECT).get(0);
        assertEquals(ItemId.ROCK, drop.itemId);
        assertEquals(2, drop.count);
    }

    @Test
    void bedFootDropsClothRangeAndLumber() {
        List<ItemStack> drops = loot.roll(BlockId.BED_FOOT, Layer.OBJECT);
        assertEquals(2, drops.size());
        assertEquals(ItemId.LUMBER, drops.get(0).itemId);
        assertEquals(1, drops.get(0).count);
        assertEquals(ItemId.CLOTH, drops.get(1).itemId);
        assertTrue(drops.get(1).count >= 1 && drops.get(1).count <= 2);
    }

    @Test
    void bedHeadDropsNothing() {
        assertTrue(loot.roll(BlockId.BED_HEAD, Layer.OBJECT).isEmpty());
    }

    @Test
    void crateHardwareIsChanceDrop() {
        LootTable withSeed = new LootTable(new Random(12345L));
        boolean sawHardware = false;
        boolean sawNoHardware = false;
        for (int i = 0; i < 20; i++) {
            List<ItemStack> drops = withSeed.roll(BlockId.CRATE, Layer.OBJECT);
            boolean hasHardware = drops.stream().anyMatch(stack -> stack.itemId == ItemId.HARDWARE);
            sawHardware |= hasHardware;
            sawNoHardware |= !hasHardware;
        }
        assertTrue(sawHardware);
        assertTrue(sawNoHardware);
        assertFalse(!sawHardware || !sawNoHardware);
    }
}
