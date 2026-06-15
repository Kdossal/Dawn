package com.dawn.world.light;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.config.GameConfig;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class LightEngineTest {
    @Test
    void singleLantern_isBrightestAtSource() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float center = world.lightMap().sample(8, 8);
        float nearby = world.lightMap().sample(9, 8);
        float far = world.lightMap().sample(8, 13);

        assertTrue(center > nearby);
        assertTrue(nearby > far);
        assertEquals(0f, world.lightMap().sample(8, 21));
    }

    @Test
    void twoLanterns_overlapUsesMax() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(4, 8, BlockId.LANTERN);
        world.setObject(12, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float mid = world.lightMap().sample(8, 8);
        float left = world.lightMap().sample(4, 8);
        float right = world.lightMap().sample(12, 8);

        assertTrue(mid > 0f);
        assertEquals(left, right, 0.001f);
    }

    @Test
    void removingLantern_clearsLightAfterRebuild() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);
        assertTrue(world.lightMap().sample(8, 8) > 0f);

        world.setObject(8, 8, BlockId.AIR);
        LightEngine.rebuildFull(world);
        assertEquals(0f, world.lightMap().sample(8, 8));
    }

    @Test
    void partialRebuild_matchesFullRebuildNearEdit() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);
        float expected = world.lightMap().sample(9, 8);

        world.lightMap().markDirty(8, 8);
        int[] bounds = world.lightMap().pollRebuildBounds();
        LightEngine.rebuild(world, bounds[0], bounds[1], bounds[2], bounds[3]);
        assertEquals(expected, world.lightMap().sample(9, 8), 0.001f);
    }

    @Test
    void heldSource_illuminatesPlayerCell() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.lightMap().updateHeldSource(8, 8, 1.0f, 8, 1f, 1f, 1f, true);
        LightEngine.rebuildFull(world);

        assertTrue(world.lightMap().sample(8, 8) > 0f);
        assertTrue(world.lightMap().sample(10, 8) > 0f);
    }

    @Test
    void heldSourceRemoved_clearsLightAfterRebuild() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.lightMap().updateHeldSource(8, 8, 1.0f, 8, 1f, 1f, 1f, true);
        LightEngine.rebuildFull(world);
        assertTrue(world.lightMap().sample(8, 8) > 0f);

        world.lightMap().updateHeldSource(0, 0, 0f, 0, 1f, 1f, 1f, false);
        LightEngine.rebuildFull(world);
        assertEquals(0f, world.lightMap().sample(8, 8));
    }

    @Test
    void falloffRespectsConfiguredRadius() {
        GameConfig cfg = GameConfig.get();
        int radius = Math.min(BlockDefinitions.lightRadius(BlockId.LANTERN), cfg.maxLightRadius);
        int size = 2 * radius + 4;
        World world = TestWorlds.smallWalkable(size, size);
        int sx = radius + 2;
        int sy = radius + 2;
        world.setObject(sx, sy, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        assertTrue(world.lightMap().sample(sx, sy) > 0f);
        assertTrue(world.lightMap().sample(sx + radius / 2, sy) > 0f);
        assertEquals(0f, world.lightMap().sample(sx, sy + radius + 1));
        assertEquals(0f, world.lightMap().sample(sx + radius + 1, sy));
    }

    @Test
    void diagonalNeighbor_receivesLight() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        assertTrue(world.lightMap().sample(9, 9) > 0f);
    }

    @Test
    void diagonalFalloff_weakerThanCardinalAtEqualChebyshev() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float cardinal = world.lightMap().sample(10, 8);
        float diagonal = world.lightMap().sample(10, 10);

        assertTrue(cardinal > diagonal, "cardinal path should retain more strength at Chebyshev distance 2");
    }

    @Test
    void cornerCut_blocksDirectDiagonalFromSource() {
        World open = TestWorlds.smallWalkable(16, 16);
        open.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(open);
        float openDiagonal = open.lightMap().sample(9, 9);

        World blocked = TestWorlds.smallWalkable(16, 16);
        blocked.setObject(8, 8, BlockId.LANTERN);
        blocked.setObject(9, 8, BlockId.CRATE);
        blocked.setObject(8, 9, BlockId.CRATE);
        LightEngine.rebuildFull(blocked);
        float cornerDiagonal = blocked.lightMap().sample(9, 9);

        assertEquals(
                BlockDefinitions.lightEmission(BlockId.LANTERN) * GameConfig.get().lightDiagonalFalloff,
                openDiagonal,
                0.01f);
        assertTrue(cornerDiagonal < openDiagonal, "corner walls should block the direct diagonal path");
    }

    @Test
    void partialRebuild_sourceOutsideWriteBounds_stillRelightsNearbyCells() {
        World world = TestWorlds.smallWalkable(64, 64);
        world.setObject(30, 30, BlockId.LANTERN);
        LightEngine.rebuildFull(world);
        assertTrue(world.lightMap().sample(20, 20) > 0f, "full rebuild should light cells within lantern radius");

        LightEngine.rebuild(world, 0, 0, 25, 25);
        assertTrue(
                world.lightMap().sample(20, 20) > 0f,
                "partial rebuild must re-propagate light from sources outside write bounds");
    }

    @Test
    void multiplicativeFalloff_softTail() {
        GameConfig cfg = GameConfig.get();
        int radius = Math.min(BlockDefinitions.lightRadius(BlockId.LANTERN), cfg.maxLightRadius);
        World world = TestWorlds.smallWalkable(32, 32);
        world.setObject(16, 16, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float edge = world.lightMap().sample(16 + radius / 2, 16);
        assertTrue(edge > cfg.lightMinThreshold, "cell inside radius should exceed min threshold");
        assertTrue(edge < BlockDefinitions.lightEmission(BlockId.LANTERN), "interior cell should be dimmer than source");
    }

    @Test
    void coloredSource_storesWeightedColorAtSource() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float[] color = world.lightMap().sampleColor(8, 8);
        assertEquals(BlockDefinitions.lightColorR(BlockId.LANTERN), color[0], 0.01f);
        assertEquals(BlockDefinitions.lightColorG(BlockId.LANTERN), color[1], 0.01f);
        assertEquals(BlockDefinitions.lightColorB(BlockId.LANTERN), color[2], 0.01f);
    }

    @Test
    void colorPropagatesToNeighbor() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(world);

        float[] neighbor = world.lightMap().sampleColor(9, 8);
        assertEquals(BlockDefinitions.lightColorR(BlockId.LANTERN), neighbor[0], 0.01f);
        assertEquals(BlockDefinitions.lightColorG(BlockId.LANTERN), neighbor[1], 0.01f);
        assertEquals(BlockDefinitions.lightColorB(BlockId.LANTERN), neighbor[2], 0.01f);
    }

    @Test
    void twoColoredSources_overlapBlendsColor() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.setObject(4, 8, BlockId.LANTERN);
        world.lightMap().updateHeldSource(12, 8, 0.8f, 24, 0.6f, 0.8f, 1.0f, true);
        LightEngine.rebuildFull(world);

        float[] blended = world.lightMap().sampleColor(8, 8);
        assertTrue(blended[0] > blended[2], "warm left and cool right should blend with red above blue at midpoint");
        assertTrue(blended[1] > 0.7f, "green channel should stay strong in warm/cool blend");
    }

    @Test
    void stoneWall_blocksLightAlongCardinalPath() {
        World open = TestWorlds.smallWalkable(16, 16);
        open.setObject(8, 8, BlockId.LANTERN);
        LightEngine.rebuildFull(open);
        float openEast = open.lightMap().sample(9, 8);

        World blocked = TestWorlds.smallWalkable(16, 16);
        blocked.setObject(8, 8, BlockId.LANTERN);
        blocked.setObject(9, 8, BlockId.STONE_WALL);
        LightEngine.rebuildFull(blocked);
        float blockedEast = blocked.lightMap().sample(10, 8);

        assertTrue(openEast > 0f);
        assertTrue(blockedEast < openEast, "stone wall should occlude light along cardinal path");
    }

    @Test
    void lightBlocker_receivesLightOnItsOwnCell() {
        GameConfig cfg = GameConfig.get();
        float expected =
                BlockDefinitions.lightEmission(BlockId.LANTERN) * cfg.lightCardinalFalloff;

        World wall = TestWorlds.smallWalkable(16, 16);
        wall.setObject(8, 8, BlockId.LANTERN);
        wall.setObject(9, 8, BlockId.STONE_WALL);
        LightEngine.rebuildFull(wall);
        assertEquals(expected, wall.lightMap().sample(9, 8), 0.01f);

        World crate = TestWorlds.smallWalkable(16, 16);
        crate.setObject(8, 8, BlockId.LANTERN);
        crate.setObject(9, 8, BlockId.CRATE);
        LightEngine.rebuildFull(crate);
        assertEquals(expected, crate.lightMap().sample(9, 8), 0.01f);
    }
}
