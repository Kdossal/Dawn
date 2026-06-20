package com.dawn.item;

import com.dawn.config.GameConfig;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.InteractionTag;
import com.dawn.world.structure.StructureKind;
import java.util.EnumMap;
import java.util.Map;

public final class ItemRegistry {
    private static final Map<ItemId, ItemDef> DEFS = new EnumMap<>(ItemId.class);

    static {
        register(tool(ItemId.HAMMER, "Hammer", "hammer", InteractionTag.BREAK, 2f));
        register(tool(ItemId.SAW, "Saw", "saw", InteractionTag.CHOP, 2f));
        register(tool(ItemId.SHOVEL, "Shovel", "shovel", InteractionTag.DIG, 2f));
        register(tiered(ItemId.ROCK, "Rock", "rock", ItemWeightTier.NORMAL));
        register(new ItemDef(ItemId.LOG, "Log", "log", 4, ItemWeightTier.NORMAL.weightPerItem, 0f, null, 0, 0, null, null));
        register(tiered(ItemId.DIRT, "Dirt", "dirt", ItemWeightTier.NORMAL));
        register(tiered(ItemId.SAND, "Sand", "sand", ItemWeightTier.NORMAL));
        register(tiered(ItemId.LUMBER, "Lumber", "lumber", ItemWeightTier.NORMAL));
        register(tiered(ItemId.HARDWARE, "Hardware", "hardware", ItemWeightTier.SMALL));
        register(tiered(ItemId.CLOTH, "Cloth", "cloth", ItemWeightTier.SMALL));
        register(placeable(
                ItemId.STONE_GROUND,
                "Stone Ground",
                "stone_ground",
                new Placeable.Ground(BlockId.STONE_GROUND),
                ItemWeightTier.NORMAL));
        register(placeable(
                ItemId.STONE_WALL,
                "Stone Wall",
                "stone_wall",
                new Placeable.Block(BlockId.STONE_WALL),
                ItemWeightTier.NORMAL));
        register(placeable(ItemId.CRATE, "Crate", "crate", new Placeable.Block(BlockId.CRATE), ItemWeightTier.LARGE));
        register(placeable(
                ItemId.LANTERN, "Lantern", "lantern", new Placeable.Block(BlockId.LANTERN), ItemWeightTier.SMALL));
        register(placeable(ItemId.BED, "Bed", "bed", new Placeable.Structure(StructureKind.BED), ItemWeightTier.LARGE));
        register(placeable(
                ItemId.OAK_SAPLING,
                "Oak Sapling",
                "oak_sapling",
                new Placeable.Block(BlockId.OAK_TREE),
                ItemWeightTier.SMALL));
        register(placeable(
                ItemId.SPRUCE_SAPLING,
                "Spruce Sapling",
                "spruce_sapling",
                new Placeable.Block(BlockId.SPRUCE_TREE),
                ItemWeightTier.SMALL));
        register(edible(ItemId.CANNED_FOOD, "Canned Food", "canned_food", ItemWeightTier.SMALL, 50f));
        register(new ItemDef(
                ItemId.LEATHER_HOOD,
                "Leather Hood",
                "leather_cap",
                1,
                1f,
                0f,
                null,
                0,
                0,
                null,
                EquipmentSlot.HEAD));
    }

    private ItemRegistry() {}

    private static ItemDef tool(ItemId id, String name, String icon, InteractionTag tag, float weight) {
        float weaponDamage = GameConfig.get().defaultToolWeaponDamage;
        return new ItemDef(id, name, icon, 1, weight, 0f, tag, 3, weaponDamage, null, null);
    }

    private static ItemDef tiered(ItemId id, String name, String icon, ItemWeightTier tier) {
        return new ItemDef(id, name, icon, tier.maxStack, tier.weightPerItem, 0f, null, 0, 0, null, null);
    }

    private static ItemDef edible(ItemId id, String name, String icon, ItemWeightTier tier, float hungerRestore) {
        return new ItemDef(
                id, name, icon, tier.maxStack, tier.weightPerItem, hungerRestore, null, 0, 0, null, null);
    }

    private static ItemDef placeable(ItemId id, String name, String icon, Placeable placeable, ItemWeightTier tier) {
        return new ItemDef(
                id, name, icon, tier.maxStack, tier.weightPerItem, 0f, null, 3, 0, placeable, null);
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
