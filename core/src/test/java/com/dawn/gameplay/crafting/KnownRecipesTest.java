package com.dawn.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KnownRecipesTest {
    @Test
    void starterKnowsAllRegisteredRecipes() {
        KnownRecipes known = new KnownRecipes();
        assertTrue(known.knows(RecipeId.CAMPFIRE));
        assertTrue(known.knows(RecipeId.BANDAGE));
    }

    @Test
    void learnAddsRecipe() {
        KnownRecipes known = new KnownRecipes();
        known.learn(RecipeId.CAMPFIRE);
        assertTrue(known.knows(RecipeId.CAMPFIRE));
    }

    @Test
    void snapshotIsUnmodifiableView() {
        KnownRecipes known = new KnownRecipes();
        assertFalse(known.snapshot().isEmpty());
    }
}
