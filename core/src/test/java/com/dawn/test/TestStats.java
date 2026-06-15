package com.dawn.test;

import com.dawn.entity.AttributeId;
import com.dawn.entity.Stats;

public final class TestStats {
    private TestStats() {}

    public static Stats withBase(
            int vitality, int brawn, int agility, int focus, int intellect, int arcana) {
        Stats stats = new Stats();
        stats.setBase(AttributeId.VITALITY, vitality);
        stats.setBase(AttributeId.BRAWN, brawn);
        stats.setBase(AttributeId.AGILITY, agility);
        stats.setBase(AttributeId.FOCUS, focus);
        stats.setBase(AttributeId.INTELLECT, intellect);
        stats.setBase(AttributeId.ARCANA, arcana);
        return stats;
    }

    public static Stats uniform(int value) {
        return withBase(value, value, value, value, value, value);
    }
}
