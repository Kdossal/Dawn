package com.dawn.gameplay.crafting;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;

public record Recipe(
        RecipeId id,
        RecipeContext context,
        String displayName,
        ItemId iconItemId,
        ItemStack[] costs,
        RecipeResult result,
        float craftTimeSec) {}
