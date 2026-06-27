package com.dawn.gameplay.drops;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class LootTable {
    public record DropContext(BlockId blockId, Layer layer) {}

    private record DropEntry(ItemId itemId, int minCount, int maxCount, float chance) {}

    private final Map<Layer, Map<BlockId, List<DropEntry>>> rules = new EnumMap<>(Layer.class);
    private final Random random;

    public LootTable() {
        this(new Random());
    }

    LootTable(Random random) {
        this.random = random;
        registerRules();
    }

    public List<ItemStack> roll(DropContext context) {
        if (context == null) {
            return List.of();
        }
        Map<BlockId, List<DropEntry>> byBlock = rules.get(context.layer());
        if (byBlock == null) {
            return List.of();
        }
        List<DropEntry> entries = byBlock.get(context.blockId());
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        List<ItemStack> drops = new ArrayList<>();
        for (DropEntry entry : entries) {
            if (entry.chance < 1f && random.nextFloat() >= entry.chance) {
                continue;
            }
            int count = rollCount(entry.minCount, entry.maxCount);
            if (count > 0) {
                drops.add(ItemStack.of(entry.itemId, count));
            }
        }
        return drops;
    }

    public List<ItemStack> roll(BlockId blockId, Layer layer) {
        return roll(new DropContext(blockId, layer));
    }

    private int rollCount(int minCount, int maxCount) {
        if (maxCount <= minCount) {
            return Math.max(minCount, 0);
        }
        return minCount + random.nextInt(maxCount - minCount + 1);
    }

    private void registerRules() {
        // Ground digs: convert terrain into base resources.
        rule(Layer.GROUND, BlockId.DIRT_GROUND, drop(ItemId.DIRT, 1));
        rule(Layer.GROUND, BlockId.SAND_GROUND, drop(ItemId.SAND, 1));
        rule(Layer.GROUND, BlockId.STONE_GROUND, drop(ItemId.ROCK, 2));

        // Object breaks/chops/grabs.
        rule(Layer.OBJECT, BlockId.ROCK, drop(ItemId.ROCK, 1));
        rule(Layer.OBJECT, BlockId.OAK_TREE, drop(ItemId.LOG, 2));
        rule(Layer.OBJECT, BlockId.OAK_STUMP, drop(ItemId.LOG, 2));
        rule(Layer.OBJECT, BlockId.SPRUCE_TREE, drop(ItemId.LOG, 2));
        rule(Layer.OBJECT, BlockId.SPRUCE_STUMP, drop(ItemId.LOG, 2));
        rule(Layer.OBJECT, BlockId.CRATE, drop(ItemId.LUMBER, 1), drop(ItemId.HARDWARE, 1, 0.5f));
        rule(Layer.OBJECT, BlockId.BED_FOOT, drop(ItemId.LUMBER, 1), dropRange(ItemId.CLOTH, 1, 2));
        rule(Layer.OBJECT, BlockId.BED_HEAD);
        rule(Layer.OBJECT, BlockId.STONE_WALL, drop(ItemId.ROCK, 2));
        rule(Layer.OBJECT, BlockId.LANTERN, drop(ItemId.LANTERN, 1));
        rule(Layer.OBJECT, BlockId.CAMPFIRE);
        rule(Layer.OBJECT, BlockId.BUSH);
    }

    private void rule(Layer layer, BlockId blockId, DropEntry... entries) {
        rules.computeIfAbsent(layer, ignored -> new EnumMap<>(BlockId.class))
                .put(blockId, List.of(entries));
    }

    private static DropEntry drop(ItemId itemId, int count) {
        return new DropEntry(itemId, count, count, 1f);
    }

    private static DropEntry drop(ItemId itemId, int count, float chance) {
        return new DropEntry(itemId, count, count, chance);
    }

    private static DropEntry dropRange(ItemId itemId, int minCount, int maxCount) {
        return new DropEntry(itemId, minCount, maxCount, 1f);
    }
}
