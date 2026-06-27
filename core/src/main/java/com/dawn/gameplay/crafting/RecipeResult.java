package com.dawn.gameplay.crafting;

import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;

/** What a completed craft produces. */
public sealed interface RecipeResult {
    record Place(Placeable placeable) implements RecipeResult {}

    record Grab(ItemStack stack) implements RecipeResult {}
}
