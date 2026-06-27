package com.dawn.inventory;

import com.dawn.entity.Entity;
import com.dawn.entity.AttributeId;
import com.dawn.gameplay.crafting.KnownRecipes;

/** Player progression and UI sheet data for the equipment tab. */
public final class PlayerProfile {
    public String name = "Traveler";
    public int level = 1;
    public int exp = 40;
    public int expToNext = 100;

    private Entity entity;
    private final KnownRecipes knownRecipes = new KnownRecipes();

    public void bindEntity(Entity entity) {
        this.entity = entity;
    }

    public KnownRecipes knownRecipes() {
        return knownRecipes;
    }

    public float expRatio() {
        return expToNext <= 0 ? 0f : Math.min(1f, exp / (float) expToNext);
    }

    public int attributeValue(int index) {
        if (entity == null || index < 0 || index >= AttributeId.ALL.length) {
            return 0;
        }
        return entity.getStats().get(AttributeId.ALL[index]);
    }
}
