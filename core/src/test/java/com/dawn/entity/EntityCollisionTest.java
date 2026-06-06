package com.dawn.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.EntityRegistry;
import org.junit.jupiter.api.Test;

class EntityCollisionTest {
    @Test
    void overlapsCell_singleCellFootprint() {
        EntityBounds box =
                EntityBounds.fromFeet(
                        EntityRegistry.get(com.dawn.entity.EntityId.PLAYER),
                        5.5f,
                        3f,
                        0,
                        0);
        assertTrue(EntityCollision.overlapsCell(box, 5, 3));
        assertFalse(EntityCollision.overlapsCell(box, 6, 3));
    }

    @Test
    void cellMinMax_atBoundaries() {
        assertEquals(5, EntityCollision.cellMin(5.0f + EntityCollision.CELL_EPS));
        assertEquals(5, EntityCollision.cellMax(5.999f - EntityCollision.CELL_EPS));
    }
}
