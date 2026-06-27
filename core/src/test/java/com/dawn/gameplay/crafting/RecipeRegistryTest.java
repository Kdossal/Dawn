package com.dawn.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class RecipeRegistryTest {
    @Test
    void campfireRecipe() {
        Recipe recipe = RecipeRegistry.get(RecipeId.CAMPFIRE);
        assertEquals(RecipeContext.HAND, recipe.context());
        assertEquals(ItemId.CAMPFIRE, recipe.iconItemId());
        assertEquals(1, recipe.costs().length);
        assertEquals(ItemId.LOG, recipe.costs()[0].itemId);
        assertEquals(2, recipe.costs()[0].count);
        assertEquals(3f, recipe.craftTimeSec(), 0.001f);
        RecipeResult.Place place = assertInstanceOf(RecipeResult.Place.class, recipe.result());
        Placeable.Block block = assertInstanceOf(Placeable.Block.class, place.placeable());
        assertEquals(BlockId.CAMPFIRE, block.blockId());
    }

    @Test
    void bandageRecipe() {
        Recipe recipe = RecipeRegistry.get(RecipeId.BANDAGE);
        assertEquals(RecipeContext.HAND, recipe.context());
        assertEquals(ItemId.CLOTH, recipe.costs()[0].itemId);
        assertEquals(2, recipe.costs()[0].count);
        RecipeResult.Grab grab = assertInstanceOf(RecipeResult.Grab.class, recipe.result());
        assertEquals(ItemId.BANDAGE, grab.stack().itemId);
        assertEquals(1, grab.stack().count);
    }

    @Test
    void dirtGroundRecipe() {
        Recipe recipe = RecipeRegistry.get(RecipeId.DIRT_GROUND);
        assertEquals(RecipeContext.SHOVEL, recipe.context());
        assertEquals(ItemId.DIRT_GROUND, recipe.iconItemId());
        assertEquals(ItemId.DIRT, recipe.costs()[0].itemId);
        assertEquals(1, recipe.costs()[0].count);
        assertEquals(3f, recipe.craftTimeSec(), 0.001f);
        RecipeResult.Place place = assertInstanceOf(RecipeResult.Place.class, recipe.result());
        Placeable.Ground ground = assertInstanceOf(Placeable.Ground.class, place.placeable());
        assertEquals(BlockId.DIRT_GROUND, ground.blockId());
    }

    @Test
    void sandGroundRecipe() {
        Recipe recipe = RecipeRegistry.get(RecipeId.SAND_GROUND);
        assertEquals(RecipeContext.SHOVEL, recipe.context());
        assertEquals(ItemId.SAND_GROUND, recipe.iconItemId());
        assertEquals(ItemId.SAND, recipe.costs()[0].itemId);
        assertEquals(1, recipe.costs()[0].count);
        RecipeResult.Place place = assertInstanceOf(RecipeResult.Place.class, recipe.result());
        Placeable.Ground ground = assertInstanceOf(Placeable.Ground.class, place.placeable());
        assertEquals(BlockId.SAND_GROUND, ground.blockId());
    }

    @Test
    void stoneGroundRecipe() {
        Recipe recipe = RecipeRegistry.get(RecipeId.STONE_GROUND);
        assertEquals(RecipeContext.SHOVEL, recipe.context());
        assertEquals(ItemId.STONE_GROUND, recipe.iconItemId());
        assertEquals(ItemId.ROCK, recipe.costs()[0].itemId);
        assertEquals(2, recipe.costs()[0].count);
        RecipeResult.Place place = assertInstanceOf(RecipeResult.Place.class, recipe.result());
        Placeable.Ground ground = assertInstanceOf(Placeable.Ground.class, place.placeable());
        assertEquals(BlockId.STONE_GROUND, ground.blockId());
    }

    @Test
    void lumberRecipe() {
        Recipe recipe = RecipeRegistry.get(RecipeId.LUMBER);
        assertEquals(RecipeContext.SAW, recipe.context());
        assertEquals(ItemId.LUMBER, recipe.iconItemId());
        assertEquals(ItemId.LOG, recipe.costs()[0].itemId);
        assertEquals(1, recipe.costs()[0].count);
        assertEquals(3f, recipe.craftTimeSec(), 0.001f);
        RecipeResult.Grab grab = assertInstanceOf(RecipeResult.Grab.class, recipe.result());
        assertEquals(ItemId.LUMBER, grab.stack().itemId);
        assertEquals(2, grab.stack().count);
    }
}
