package com.dawn.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.config.Constants;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class EntityMovementSolverTest {
    @Test
    void movesThroughVerticalCorridorWhenCentered() {
        World world = TestWorlds.smallWalkable(5, 5);
        for (int y = 0; y < 5; y++) {
            world.setObject(1, y, BlockId.ROCK);
            world.setObject(3, y, BlockId.ROCK);
        }
        EntityDef def = EntityRegistry.get(EntityId.PLAYER);
        EntityMovementSolver.Result result = EntityMovementSolver.move(def, 2.5f, 1f, 0f, 1.5f, world);
        assertTrue(result.moved());
        assertTrue(result.feetY() > 1f);
    }

    @Test
    void horizontalMoveInTallCorridor() {
        World world = TestWorlds.smallWalkable(6, 4);
        world.setObject(2, 2, BlockId.ROCK);
        world.setObject(3, 2, BlockId.ROCK);
        EntityDef def = EntityRegistry.get(EntityId.PLAYER);
        float feetY = 1f;
        EntityMovementSolver.Result result = EntityMovementSolver.move(def, 1.5f, feetY, 1f, 0f, world);
        assertTrue(result.moved());
        assertTrue(result.feetX() > 1.5f);
    }

    @Test
    void diagonalIntoWall_doesNotGainExtraDistanceFromSnap() {
        World world = TestWorlds.smallWalkable(7, 7);
        for (int y = 0; y < 7; y++) {
            world.setObject(3, y, BlockId.ROCK);
        }
        EntityDef def = EntityRegistry.get(EntityId.PLAYER);
        float startX = 2.62f;
        float startY = 1.0f;
        float dx = 0.28f;
        float dy = 0.28f;

        EntityMovementSolver.Result result = EntityMovementSolver.move(def, startX, startY, dx, dy, world);
        float movedX = result.feetX() - startX;
        float movedY = result.feetY() - startY;
        float movedLen = (float) Math.sqrt(movedX * movedX + movedY * movedY);
        float requestedLen = (float) Math.sqrt(dx * dx + dy * dy);

        assertTrue(movedLen <= requestedLen + 0.0001f);
    }
}
