package com.dawn.entity.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatusModifiersTest {
    @Test
    void poisoned_reducesHpRegenByOne() {
        StatusSet statuses = new StatusSet();
        statuses.add(StatusId.POISONED);

        assertEquals(-0.5f, StatusModifiers.effectiveHpRegenPerSec(0.5f, statuses), 0.001f);
    }

    @Test
    void starving_reducesHpRegenByPointSix() {
        StatusSet statuses = new StatusSet();
        statuses.add(StatusId.STARVING);

        assertEquals(-0.1f, StatusModifiers.effectiveHpRegenPerSec(0.5f, statuses), 0.001f);
    }

    @Test
    void poisonedAndStarving_stackPenalties() {
        StatusSet statuses = new StatusSet();
        statuses.add(StatusId.POISONED);
        statuses.add(StatusId.STARVING);

        assertEquals(0.4f, StatusModifiers.effectiveHpRegenPerSec(2f, statuses), 0.001f);
    }

    @Test
    void hungry_halvesEnergyRegen() {
        StatusSet statuses = new StatusSet();
        statuses.add(StatusId.HUNGRY);

        assertEquals(2.5f, StatusModifiers.effectiveEnergyRegenPerSec(5f, statuses), 0.001f);
    }
}
