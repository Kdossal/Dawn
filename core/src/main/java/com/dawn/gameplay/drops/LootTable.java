package com.dawn.gameplay.drops;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import java.util.Collections;
import java.util.List;

public final class LootTable {
    public List<ItemStack> roll(BlockId blockId, Layer layer) {
        if (layer == Layer.OBJECT) {
            return switch (blockId) {
                case ROCK -> List.of(ItemStack.of(ItemId.ROCK));
                case OAK_TREE, OAK_STUMP, SPRUCE_TREE, SPRUCE_STUMP ->
                        List.of(ItemStack.of(ItemId.WOOD, 2));
                case CRATE -> List.of(ItemStack.of(ItemId.CRATE));
                case STONE_WALL -> List.of(ItemStack.of(ItemId.STONE));
                case LANTERN -> List.of(ItemStack.of(ItemId.LANTERN));
                case BED_FOOT -> List.of(ItemStack.of(ItemId.BED));
                case BED_HEAD -> Collections.emptyList();
                case BUSH -> Collections.emptyList();
                default -> Collections.emptyList();
            };
        }
        if (layer == Layer.GROUND) {
            return switch (blockId) {
                case DIRT -> List.of(ItemStack.of(ItemId.DIRT_CLUMP));
                case SAND -> List.of(ItemStack.of(ItemId.SAND));
                case STONE -> List.of(ItemStack.of(ItemId.STONE));
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }
}
