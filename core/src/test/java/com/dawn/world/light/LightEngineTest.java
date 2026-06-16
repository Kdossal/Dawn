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

        assertTrue(center >= nearby);
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

        assertTrue(openDiagonal > 0f, "open diagonal should receive light");
        assertTrue(cornerDiagonal <= openDiagonal, "corner walls should not amplify the direct diagonal path");
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
    void overlapCornerColor_tracksDominantLevelWinner() {
        World world = TestWorlds.smallWalkable(24, 24);
        world.setObject(6, 12, BlockId.LANTERN);
        world.lightMap().updateHeldSource(16, 12, 0.6f, 24, 0.35f, 0.65f, 1.0f, true);
        LightEngine.rebuildFull(world);

        float[] nearWarm = world.lightMap().sampleCornerColor(9, 12);
        float[] nearCool = world.lightMap().sampleCornerColor(14, 12);

        assertTrue(nearWarm[0] > nearWarm[2], "near warm source, red should dominate blue");
        assertTrue(nearCool[2] > nearCool[0], "near cool source, blue should dominate red");
    }

    @Test
    void heldSourceColorChangeWithoutMove_marksDirtyAndRebuildsColor() {
        World world = TestWorlds.smallWalkable(16, 16);
        world.lightMap().updateHeldSource(8, 8, 1.0f, 10, 1f, 0.3f, 0.2f, true);
        LightEngine.rebuildFull(world);
        float[] warm = world.lightMap().sampleColor(8, 8);

        world.lightMap().updateHeldSource(8, 8, 1.0f, 10, 0.2f, 0.5f, 1f, true);
        int[] bounds = world.lightMap().pollRebuildBounds();
        LightEngine.rebuild(world, bounds[0], bounds[1], bounds[2], bounds[3]);
        float[] cool = world.lightMap().sampleColor(8, 8);

        assertTrue(warm[0] > warm[2], "initial held source should look warm");
        assertTrue(cool[2] > cool[0], "updated held source should rebuild to cool hue");
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
    void enclosedLantern_leaksThroughStoneWallsWeakly() {
        World world = TestWorlds.smallWalkable(16, 16);
        int sx = 8;
        int sy = 8;
        world.setObject(sx, sy, BlockId.LANTERN);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                world.setObject(sx + dx, sy + dy, BlockId.STONE_WALL);
            }
        }
        LightEngine.rebuildFull(world);

        float inside = world.lightMap().sample(sx, sy);
        float outside = world.lightMap().sample(sx + 2, sy);
        assertTrue(inside > 0f, "enclosed lantern should still light its own cell");
        // Stone walls transmit 10% so a little light leaks, but the interior must be much brighter.
        assertTrue(inside > outside * 5f, "interior must be substantially brighter than exterior through stone wall");
    }

    @Test
    void enclosedLeakIsSymmetric_acrossAllFourDiagonals() {
        World world = TestWorlds.smallWalkable(16, 16);
        int sx = 8;
        int sy = 8;
        world.setObject(sx, sy, BlockId.LANTERN);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                world.setObject(sx + dx, sy + dy, BlockId.STONE_WALL);
            }
        }
        LightEngine.rebuildFull(world);

        float ne = world.lightMap().sample(sx + 2, sy + 2);
        float nw = world.lightMap().sample(sx - 2, sy + 2);
        float se = world.lightMap().sample(sx + 2, sy - 2);
        float sw = world.lightMap().sample(sx - 2, sy - 2);
        // Transmission is symmetric so all four diagonal corners must be equal.
        assertEquals(ne, nw, 0.001f, "diagonal transmission must not be biased toward one side");
        assertEquals(ne, se, 0.001f, "diagonal transmission must not be biased toward one side");
        assertEquals(ne, sw, 0.001f, "diagonal transmission must not be biased toward one side");
    }

    @Test
    void partialTransmission_enclosedCrateLeaksMoreThanStoneWall() {
        // A single-cell blocker can be bypassed diagonally, so we test enclosures where
        // all paths out must cross the ring.
        int sx = 8, sy = 8;

        World crateWorld = TestWorlds.smallWalkable(16, 16);
        crateWorld.setObject(sx, sy, BlockId.LANTERN);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                crateWorld.setObject(sx + dx, sy + dy, BlockId.CRATE);
            }
        }
        LightEngine.rebuildFull(crateWorld);
        float crateOutside = crateWorld.lightMap().sample(sx + 2, sy);

        World wallWorld = TestWorlds.smallWalkable(16, 16);
        wallWorld.setObject(sx, sy, BlockId.LANTERN);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                wallWorld.setObject(sx + dx, sy + dy, BlockId.STONE_WALL);
            }
        }
        LightEngine.rebuildFull(wallWorld);
        float wallOutside = wallWorld.lightMap().sample(sx + 2, sy);

        assertTrue(crateOutside > wallOutside, "50% crate ring should leak more light than 10% stone wall ring");
        assertTrue(wallOutside > 0f, "stone wall ring (10% transmission) should still let some light through");
    }

    @Test
    void lightBlocker_receivesLightOnItsOwnCell() {
        World wall = TestWorlds.smallWalkable(16, 16);
        wall.setObject(8, 8, BlockId.LANTERN);
        wall.setObject(9, 8, BlockId.STONE_WALL);
        LightEngine.rebuildFull(wall);
        assertTrue(wall.lightMap().sample(9, 8) > 0f, "stone wall cell should receive incident light");

        World crate = TestWorlds.smallWalkable(16, 16);
        crate.setObject(8, 8, BlockId.LANTERN);
        crate.setObject(9, 8, BlockId.CRATE);
        LightEngine.rebuildFull(crate);
        assertTrue(crate.lightMap().sample(9, 8) > 0f, "crate cell should receive incident light");
    }
}
