package com.dawn.gameplay.crafting;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Recipes the player has learned (in-memory until save system exists). */
public final class KnownRecipes {
    private final Set<RecipeId> known = EnumSet.noneOf(RecipeId.class);

    public KnownRecipes() {
        for (RecipeId id : RecipeId.values()) {
            known.add(id);
        }
    }

    public boolean knows(RecipeId id) {
        return id != null && known.contains(id);
    }

    public void learn(RecipeId id) {
        if (id != null) {
            known.add(id);
        }
    }

    public Set<RecipeId> snapshot() {
        return Collections.unmodifiableSet(EnumSet.copyOf(known));
    }
}
