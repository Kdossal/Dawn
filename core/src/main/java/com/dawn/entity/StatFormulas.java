package com.dawn.entity;

import com.dawn.config.GameConfig;

/** Derived gameplay values from entity attributes. Tune rates via {@link GameConfig}. */
public final class StatFormulas {
    private StatFormulas() {}

    public static float maxHealth(Stats stats) {
        int vitality = stats.get(AttributeId.VITALITY);
        int brawn = stats.get(AttributeId.BRAWN);
        return 2f * vitality + (float) Math.floor(brawn / 2.0);
    }

    public static float maxEnergy(Stats stats) {
        int vitality = stats.get(AttributeId.VITALITY);
        return (float) Math.floor(2.5 * vitality);
    }

    public static float armor(Stats stats) {
        int vitality = stats.get(AttributeId.VITALITY);
        return (float) Math.floor(vitality / 4.0);
    }

    public static int maxCarryWeight(Stats stats) {
        return stats.get(AttributeId.BRAWN) * 2;
    }

    public static float moveSpeedCellsPerSec(Stats stats) {
        return moveSpeedCellsPerSec(stats, false);
    }

    public static float moveSpeedCellsPerSec(Stats stats, boolean running) {
        int agility = stats.get(AttributeId.AGILITY);
        float speed = (float) Math.floor(Math.pow(agility, 0.7));
        if (running) {
            GameConfig config = GameConfig.get();
            speed *= 1f + config.runSpeedBonusPercent / 100f;
        }
        return speed;
    }

    public static float dodgePercent(Stats stats) {
        return stats.get(AttributeId.AGILITY) / 2f;
    }

    /** Bonus added to weapon DPS for melee hits and matching tool purpose breaks. */
    public static float meleeBonus(Stats stats) {
        return (float) Math.floor(stats.get(AttributeId.BRAWN) / 2.0);
    }

    /** DPS for grab/harvest interactions (Focus only). */
    public static float grabDamagePerSec(Stats stats) {
        return (float) Math.ceil(stats.get(AttributeId.FOCUS) / 5.0);
    }

    public static float hpRegenPerSec() {
        return GameConfig.get().hpRegenPerSec;
    }

    public static float energyRegenPerSec() {
        return GameConfig.get().energyRegenPerSec;
    }

    public static float runEnergyDrainPerSec() {
        return GameConfig.get().runEnergyDrainPerSec;
    }

    public static float hungerDrainPerSec() {
        return GameConfig.get().hungerDrainPerSec;
    }

    public static float thirstDrainPerSec() {
        return GameConfig.get().thirstDrainPerSec;
    }
}
