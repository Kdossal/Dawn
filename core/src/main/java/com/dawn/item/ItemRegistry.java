package com.dawn.item;

import com.dawn.inventory.EquipmentSlot;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.InteractionTag;
import com.dawn.world.structure.StructureKind;
import java.util.EnumMap;
import java.util.Map;

public final class ItemRegistry {
    private static final Map<ItemId, ItemDef> DEFS = new EnumMap<>(ItemId.class);

    static {
        register(tool(ItemId.PICKAXE, "Pickaxe", "pickaxe", InteractionTag.MINE));
        register(tool(ItemId.AXE, "Axe", "axe", InteractionTag.CHOP));
        register(tool(ItemId.SHOVEL, "Shovel", "shovel", InteractionTag.DIG));
        register(new ItemDef(ItemId.ROCK, "Rock", "rock", 64, null, 0, 0, null, null));
        register(new ItemDef(ItemId.WOOD, "Wood", "wood", 64, null, 0, 0, null, null));
        register(placeable(ItemId.DIRT_CLUMP, "Dirt", "dirt_clump", new Placeable.Ground(BlockId.DIRT)));
        register(placeable(ItemId.SAND, "Sand", "sand", new Placeable.Ground(BlockId.SAND)));
        register(placeable(ItemId.CRATE, "Crate", "crate", new Placeable.Block(BlockId.CRATE)));
        register(placeable(ItemId.BED, "Bed", "bed", new Placeable.Structure(StructureKind.BED)));
        register(placeable(
                ItemId.OAK_SAPLING, "Oak Sapling", "oak_sapling", new Placeable.Block(BlockId.OAK_TREE)));
        register(placeable(
                ItemId.SPRUCE_SAPLING,
                "Spruce Sapling",
                "spruce_sapling",
                new Placeable.Block(BlockId.SPRUCE_TREE)));
        register(new ItemDef(
                ItemId.LEATHER_CAP, "Leather Cap", "leather_cap", 1, null, 0, 0, null, EquipmentSlot.HEAD));
    }

    private ItemRegistry() {}

    private static ItemDef tool(ItemId id, String name, String icon, InteractionTag tag) {
        return new ItemDef(id, name, icon, 1, tag, 3, 100, null, null);
    }

    private static ItemDef placeable(ItemId id, String name, String icon, Placeable placeable) {
        return new ItemDef(id, name, icon, 64, null, 3, 0, placeable, null);
    }

    private static void register(ItemDef def) {
        DEFS.put(def.id(), def);
    }

    public static ItemDef get(ItemId id) {
        return id == null ? null : DEFS.get(id);
    }

    public static ItemDef get(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : get(stack.itemId);
    }

    public static ItemId[] allIds() {
        return ItemId.values();
    }
}
