package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.EntityDef;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityRegistry;
import org.junit.jupiter.api.Test;

class ReachResolverTest {
    private final EntityDef player = EntityRegistry.get(EntityId.PLAYER);

    @Test
    void radius3_symmetricCardinals_fromHitboxCenter() {
        float feetX = 10.5f;
        float feetY = 10f;
        float[] center = ReachResolver.reachCenter(player, feetX, feetY);
        int baseX = (int) Math.floor(center[0]);
        int baseY = (int) Math.floor(center[1]);

        assertTrue(ReachResolver.inReach(player, feetX, feetY, baseX + 3, baseY, 3f));
        assertTrue(ReachResolver.inReach(player, feetX, feetY, baseX - 3, baseY, 3f));
        assertTrue(ReachResolver.inReach(player, feetX, feetY, baseX, baseY + 3, 3f));
        assertTrue(ReachResolver.inReach(player, feetX, feetY, baseX, baseY - 3, 3f));
    }

    @Test
    void radius3_notBeyondCardinals() {
        float feetX = 10.5f;
        float feetY = 10f;
        float[] center = ReachResolver.reachCenter(player, feetX, feetY);
        int baseX = (int) Math.floor(center[0]);
        int baseY = (int) Math.floor(center[1]);

        assertFalse(ReachResolver.inReach(player, feetX, feetY, baseX + 4, baseY, 3f));
        assertFalse(ReachResolver.inReach(player, feetX, feetY, baseX, baseY + 4, 3f));
    }

    @Test
    void radius1_isPlusNotDiagonal() {
        float feetX = 5.5f;
        float feetY = 5f;
        assertTrue(ReachResolver.inReach(player, feetX, feetY, 6, 5, 1f));
        assertFalse(ReachResolver.inReach(player, feetX, feetY, 6, 6, 1f));
    }

    @Test
    void radius2_includesCardinalTwo() {
        float feetX = 5.5f;
        float feetY = 5f;
        assertTrue(ReachResolver.inReach(player, feetX, feetY, 7, 5, 2f));
        assertFalse(ReachResolver.inReach(player, feetX, feetY, 7, 7, 2f));
    }
}
