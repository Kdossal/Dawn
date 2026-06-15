package com.dawn.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class EntityPoisonTest {
    @Test
    void poisonExpiresAfterDuration() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);

        player.tickEffects(15f);

        assertFalse(player.isPoisoned());
    }
}
