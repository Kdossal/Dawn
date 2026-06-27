package com.dawn.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import java.util.List;
import org.junit.jupiter.api.Test;

class CraftingContextResolverTest {
    @Test
    void emptyHandUsesHandContext() {
        assertEquals(RecipeContext.HAND, CraftingContextResolver.contextForHeld(ItemStack.empty()));
    }

    @Test
    void hammerSelectedUsesHammerContext() {
        assertEquals(
                RecipeContext.HAMMER,
                CraftingContextResolver.contextForHeld(ItemStack.of(ItemId.HAMMER)));
    }

    @Test
    void handRecipesForEmptySelection() {
        List<Recipe> recipes =
                CraftingContextResolver.recipesFor(ItemStack.empty(), new KnownRecipes());
        assertEquals(2, recipes.size());
        assertTrue(recipes.stream().anyMatch(r -> r.id() == RecipeId.CAMPFIRE));
        assertTrue(recipes.stream().anyMatch(r -> r.id() == RecipeId.BANDAGE));
    }

    @Test
    void hammerContextHasNoRecipesYet() {
        List<Recipe> recipes =
                CraftingContextResolver.recipesFor(ItemStack.of(ItemId.HAMMER), new KnownRecipes());
        assertTrue(recipes.isEmpty());
    }

    @Test
    void shovelContextShowsThreeGroundRecipes() {
        List<Recipe> recipes =
                CraftingContextResolver.recipesFor(ItemStack.of(ItemId.SHOVEL), new KnownRecipes());
        assertEquals(3, recipes.size());
        assertTrue(recipes.stream().anyMatch(r -> r.id() == RecipeId.DIRT_GROUND));
        assertTrue(recipes.stream().anyMatch(r -> r.id() == RecipeId.SAND_GROUND));
        assertTrue(recipes.stream().anyMatch(r -> r.id() == RecipeId.STONE_GROUND));
    }

    @Test
    void sawContextShowsLumberRecipe() {
        List<Recipe> recipes =
                CraftingContextResolver.recipesFor(ItemStack.of(ItemId.SAW), new KnownRecipes());
        assertEquals(1, recipes.size());
        assertEquals(RecipeId.LUMBER, recipes.get(0).id());
    }
}
