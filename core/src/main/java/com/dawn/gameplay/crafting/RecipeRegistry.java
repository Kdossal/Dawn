package com.dawn.gameplay.crafting;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;
import com.dawn.world.block.BlockId;
import java.util.EnumMap;
import java.util.Map;

public final class RecipeRegistry {
    private static final Map<RecipeId, Recipe> RECIPES = new EnumMap<>(RecipeId.class);

    static {
        register(new Recipe(
                RecipeId.CAMPFIRE,
                RecipeContext.HAND,
                "Campfire",
                ItemId.CAMPFIRE,
                new ItemStack[] {ItemStack.of(ItemId.LOG, 2)},
                new RecipeResult.Place(new Placeable.Block(BlockId.CAMPFIRE)),
                3f));
        register(new Recipe(
                RecipeId.BANDAGE,
                RecipeContext.HAND,
                "Bandage",
                ItemId.BANDAGE,
                new ItemStack[] {ItemStack.of(ItemId.CLOTH, 2)},
                new RecipeResult.Grab(ItemStack.of(ItemId.BANDAGE, 1)),
                3f));
        register(new Recipe(
                RecipeId.DIRT_GROUND,
                RecipeContext.SHOVEL,
                "Dirt Ground",
                ItemId.DIRT_GROUND,
                new ItemStack[] {ItemStack.of(ItemId.DIRT, 1)},
                new RecipeResult.Place(new Placeable.Ground(BlockId.DIRT_GROUND)),
                3f));
        register(new Recipe(
                RecipeId.SAND_GROUND,
                RecipeContext.SHOVEL,
                "Sand Ground",
                ItemId.SAND_GROUND,
                new ItemStack[] {ItemStack.of(ItemId.SAND, 1)},
                new RecipeResult.Place(new Placeable.Ground(BlockId.SAND_GROUND)),
                3f));
        register(new Recipe(
                RecipeId.STONE_GROUND,
                RecipeContext.SHOVEL,
                "Stone Ground",
                ItemId.STONE_GROUND,
                new ItemStack[] {ItemStack.of(ItemId.ROCK, 2)},
                new RecipeResult.Place(new Placeable.Ground(BlockId.STONE_GROUND)),
                3f));
        register(new Recipe(
                RecipeId.LUMBER,
                RecipeContext.SAW,
                "Lumber",
                ItemId.LUMBER,
                new ItemStack[] {ItemStack.of(ItemId.LOG, 1)},
                new RecipeResult.Grab(ItemStack.of(ItemId.LUMBER, 2)),
                3f));
    }

    private RecipeRegistry() {}

    private static void register(Recipe recipe) {
        RECIPES.put(recipe.id(), recipe);
    }

    public static Recipe get(RecipeId id) {
        return id == null ? null : RECIPES.get(id);
    }

    public static RecipeId[] allIds() {
        return RecipeId.values();
    }
}
