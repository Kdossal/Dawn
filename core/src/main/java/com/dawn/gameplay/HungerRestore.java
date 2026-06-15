package com.dawn.gameplay;

import com.dawn.entity.Entity;

/** Applies food hunger restoration with halved overfill above max hunger. */
public final class HungerRestore {
    private HungerRestore() {}

    public static void apply(Entity entity, float restoreAmount) {
        if (entity == null || restoreAmount <= 0f) {
            return;
        }
        float current = entity.getCurrentHunger();
        float max = entity.getMaxHunger();
        float toMax = Math.max(0f, max - current);
        float toMaxGain = Math.min(restoreAmount, toMax);
        float remainder = restoreAmount - toMaxGain;
        float overfillGain = remainder * 0.5f;
        entity.setHunger(current + toMaxGain + overfillGain);
    }
}
