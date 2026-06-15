package com.dawn.entity.status;

public final class StatusModifiers {
    private static final float POISONED_HP_REGEN_PENALTY = 1f;
    private static final float STARVING_HP_REGEN_PENALTY = 0.6f;

    private StatusModifiers() {}

    public static float moveSpeedMultiplier(StatusSet statuses) {
        if (statuses == null) {
            return 1f;
        }
        if (statuses.has(StatusId.IMMOBILE)) {
            return 0f;
        }
        if (statuses.has(StatusId.BURDENED)) {
            return 0.5f;
        }
        return 1f;
    }

    public static float dodgeMultiplier(StatusSet statuses) {
        if (statuses == null) {
            return 1f;
        }
        if (statuses.has(StatusId.BURDENED) || statuses.has(StatusId.IMMOBILE)) {
            return 0.5f;
        }
        return 1f;
    }

    public static float effectiveHpRegenPerSec(float base, StatusSet statuses) {
        float rate = base;
        if (statuses != null) {
            if (statuses.has(StatusId.POISONED)) {
                rate -= POISONED_HP_REGEN_PENALTY;
            }
            if (statuses.has(StatusId.STARVING)) {
                rate -= STARVING_HP_REGEN_PENALTY;
            }
        }
        return rate;
    }

    public static float effectiveEnergyRegenPerSec(float base, StatusSet statuses) {
        if (statuses != null && statuses.has(StatusId.HUNGRY)) {
            return base * 0.5f;
        }
        return base;
    }
}
