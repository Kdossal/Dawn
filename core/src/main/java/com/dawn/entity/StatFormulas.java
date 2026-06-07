package com.dawn.entity;

import com.dawn.config.GameConfig;

/** Derived gameplay values from entity stats. Tune via {@link GameConfig}. */
public final class StatFormulas {
    private StatFormulas() {}

    public static float moveSpeedCellsPerSec(Stats stats) {
        return moveSpeedCellsPerSec(stats, false);
    }

    public static float moveSpeedCellsPerSec(Stats stats, boolean running) {
        GameConfig config = GameConfig.get();
        float agility = stats.get(StatId.AGILITY);
        float speed = config.baseMoveSpeed * (1f + (agility - 10f) * config.agilitySpeedFactor);
        if (running) {
            speed *= 1f + config.runSpeedBonusPercent / 100f;
        }
        return speed;
    }

    public static float maxHealth(Stats stats) {
        GameConfig config = GameConfig.get();
        return config.baseMaxHealth + stats.get(StatId.VIGOR) * config.vigorHealthFactor;
    }

    public static float armor(Stats stats) {
        GameConfig config = GameConfig.get();
        return stats.get(StatId.STRENGTH) * config.strengthArmorFactor;
    }
}
