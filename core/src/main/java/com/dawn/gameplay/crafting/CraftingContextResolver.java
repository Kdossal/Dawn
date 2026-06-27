package com.dawn.gameplay.crafting;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** Resolves which recipes appear for the current hotbar selection and known set. */
public final class CraftingContextResolver {
    private CraftingContextResolver() {}

    public static RecipeContext contextForHeld(ItemStack held) {
        if (held == null || held.isEmpty()) {
            return RecipeContext.HAND;
        }
        return switch (held.itemId) {
            case HAMMER -> RecipeContext.HAMMER;
            case SAW -> RecipeContext.SAW;
            case SHOVEL -> RecipeContext.SHOVEL;
            default -> RecipeContext.HAND;
        };
    }

    public static List<Recipe> recipesFor(ItemStack held, KnownRecipes known) {
        RecipeContext context = contextForHeld(held);
        List<Recipe> out = new ArrayList<>();
        for (RecipeId id : RecipeRegistry.allIds()) {
            if (!known.knows(id)) {
                continue;
            }
            Recipe recipe = RecipeRegistry.get(id);
            if (recipe != null && recipe.context() == context) {
                out.add(recipe);
            }
        }
        return List.copyOf(out);
    }
}
