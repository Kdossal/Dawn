package com.dawn.entity;

import com.dawn.entity.status.StatusModifiers;

/** Passive HP/energy regen and hunger/thirst drain (player only). */
public final class VitalSystem {
    private VitalSystem() {}

    public static void update(Entity entity, float delta) {
        update(entity, delta, false);
    }

    public static void update(Entity entity, float delta, boolean running) {
        if (entity == null || entity.getEntityId() != EntityId.PLAYER || delta <= 0f) {
            return;
        }
        float hpGain =
                StatusModifiers.effectiveHpRegenPerSec(
                                StatFormulas.hpRegenPerSec(), entity.getStatuses())
                        * delta;
        float energyDelta =
                running
                        ? -StatFormulas.runEnergyDrainPerSec() * delta
                        : StatusModifiers.effectiveEnergyRegenPerSec(
                                        StatFormulas.energyRegenPerSec(), entity.getStatuses())
                                * delta;
        float hungerLoss = StatFormulas.hungerDrainPerSec() * delta;
        float thirstLoss = StatFormulas.thirstDrainPerSec() * delta;

        entity.adjustVitals(hpGain, energyDelta, -hungerLoss, -thirstLoss);
    }
}
