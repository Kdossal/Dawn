package com.dawn.gameplay.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.config.Constants;
import com.dawn.config.GameConfig;
import com.dawn.render.GameSettings;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimulationSystemTest {
    private int savedMaxCatchup;
    private int savedGrassBudget;
    private int savedSimMargin;
    private int savedSpreadAttempts;

    @BeforeEach
    void saveConfig() {
        GameConfig cfg = GameConfig.get();
        savedMaxCatchup = cfg.maxCatchupTicksPerChunk;
        savedGrassBudget = cfg.maxCatchupGrassEventsPerFrame;
        savedSimMargin = cfg.simMarginCells;
        savedSpreadAttempts = cfg.grassSpreadAttemptsPerEvent;
    }

    @AfterEach
    void restoreConfig() {
        GameConfig cfg = GameConfig.get();
        cfg.maxCatchupTicksPerChunk = savedMaxCatchup;
        cfg.maxCatchupGrassEventsPerFrame = savedGrassBudget;
        cfg.simMarginCells = savedSimMargin;
        cfg.grassSpreadAttemptsPerEvent = savedSpreadAttempts;
    }

    @Test
    void catchUp_syncsLastSimulatedTickToCurrentTick() {
        World world = TestWorlds.smallClear(32, 32);
        SimulationSystem sim = new SimulationSystem(world);
        sim.advanceCurrentTickForTest(500);
        sim.setRegionLastSimulatedTickForTest(0, 0, 0);

        wakeRegion(sim, 0, 0);
        drainCatchUpFully(sim, 0, 0);

        assertEquals(sim.getCurrentTick(), sim.getRegionLastSimulatedTickForTest(0, 0));
        assertEquals(0, sim.pendingGrassEventsForTest(0, 0));
    }

    @Test
    void catchUp_capsTickEquivalentsAtMaxCatchup() {
        GameConfig.get().maxCatchupTicksPerChunk = 10_000;
        int expected = 10_000 / GameConfig.get().grassSpreadIntervalTicks;
        assertEquals(expected, new SimulationSystem(TestWorlds.smallClear(16, 16)).computeGrassCatchUpEventsForTest(1_000_000L));
    }

    @Test
    void grassSpread_doesNotWriteIntoInactiveNeighborRegion() {
        World world = TestWorlds.smallClear(32, 32);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                TestWorlds.setSolidDirt(world, x, y);
                world.setFloor(x, y, BlockId.GRASS);
            }
        }
        world.setFloor(15, 8, BlockId.GRASS);
        TestWorlds.setSolidDirt(world, 16, 8);

        SimulationSystem sim = new SimulationSystem(world);
        sim.setRegionLastSimulatedTickForTest(0, 0, 0);
        sim.advanceCurrentTickForTest(50_000);
        GameConfig.get().simMarginCells = 0;
        GameConfig.get().maxCatchupGrassEventsPerFrame = 50_000;

        wakeRegionTight(sim, 0, 0);
        drainCatchUpFully(sim, 0, 0);

        assertEquals(BlockId.AIR, world.getFloor(16, 8));
        assertEquals(BlockId.DIRT_GROUND, world.getGround(16, 8));
    }

    @Test
    void viewportBottomRow_isSimActive_atMapCorner() {
        World world = World.createDefault();
        SimulationSystem sim = new SimulationSystem(world);
        float playerX = 92f;
        float playerY = 92f;
        int bottomRowY = viewportBottomCellY(playerX, playerY);

        updateActiveRegionsLikeGame(sim, playerX, playerY, 1f);

        assertTrue(
                sim.isCellSimActive((int) playerX, bottomRowY),
                "bottom viewport row y=" + bottomRowY + " should be in an active sim region");
    }

    @Test
    void dirtStripe_greensSubstantiallyWhilePlayerStays() {
        World world = TestWorlds.smallClear(64, 64);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                TestWorlds.setSolidDirt(world, x, y);
                world.setFloor(x, y, BlockId.GRASS);
            }
        }
        int stripeY = 30;
        for (int x = 0; x < 64; x++) {
            TestWorlds.setSolidDirt(world, x, stripeY);
        }

        SimulationSystem sim = new SimulationSystem(world);
        float playerX = 32f;
        float playerY = 45f;
        int bareDirtBefore = countBareDirt(world);

        runSimSeconds(sim, world, playerX, playerY, 60f);

        int bareDirtAfter = countBareDirt(world);
        assertTrue(
                bareDirtAfter < bareDirtBefore - 8,
                "grass spread should convert many stripe cells in ~60s; bare dirt before="
                        + bareDirtBefore
                        + " after="
                        + bareDirtAfter);
    }

    @Test
    void playgroundCorner_viewportBottomDirt_notMapBorder() {
        World world = World.createDefault();
        SimulationSystem sim = new SimulationSystem(world);
        float playerX = 92f;
        float playerY = 92f;
        int bottomY = viewportBottomCellY(playerX, playerY);

        assertTrue(bottomY > 2 && bottomY < world.getHeight() - 2, "sanity: not on map dirt border");

        int bareOnBottomRow = countBareDirtOnRow(world, bottomY, playerX);
        runSimSeconds(sim, world, playerX, playerY, 90f);
        int bareAfter = countBareDirtOnRow(world, bottomY, playerX);

        assertTrue(
                bareAfter <= bareOnBottomRow,
                "bottom viewport row bare dirt should not increase; before="
                        + bareOnBottomRow
                        + " after="
                        + bareAfter);
    }

    @Test
    void frameBudget_defersRemainingCatchUpEvents() {
        World world = TestWorlds.smallClear(32, 32);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                TestWorlds.setSolidDirt(world, x, y);
                world.setFloor(x, y, BlockId.GRASS);
            }
        }

        SimulationSystem sim = new SimulationSystem(world);
        sim.setRegionLastSimulatedTickForTest(0, 0, 0);
        sim.advanceCurrentTickForTest(10_000);
        GameConfig.get().maxCatchupGrassEventsPerFrame = 5;

        wakeRegion(sim, 0, 0);
        assertTrue(sim.pendingGrassEventsForTest(0, 0) > 5);

        wakeRegion(sim, 0, 0);
        drainCatchUpFully(sim, 0, 0);

        assertEquals(0, sim.pendingGrassEventsForTest(0, 0));
        assertEquals(sim.getCurrentTick(), sim.getRegionLastSimulatedTickForTest(0, 0));
    }

    private static void wakeRegion(SimulationSystem sim, int regionX, int regionY) {
        int originCell = regionX * GameConfig.get().regionSizeCells;
        float originPx = originCell * Constants.CELL_SIZE_PX;
        float sizePx = GameConfig.get().regionSizeCells * Constants.CELL_SIZE_PX;
        float center = originCell + GameConfig.get().regionSizeCells / 2f;
        sim.updateActiveRegions(originPx, originPx, originPx + sizePx, originPx + sizePx, center, center);
    }

    private static void wakeRegionTight(SimulationSystem sim, int regionX, int regionY) {
        int rs = GameConfig.get().regionSizeCells;
        int originCell = regionX * rs;
        int lastCell = originCell + rs - 1;
        float originPx = originCell * Constants.CELL_SIZE_PX;
        float rightPx = lastCell * Constants.CELL_SIZE_PX;
        float topPx = lastCell * Constants.CELL_SIZE_PX;
        float center = originCell + rs / 2f;
        sim.updateActiveRegions(originPx, originPx, rightPx, topPx, center, center);
    }

    private static void drainCatchUpFully(SimulationSystem sim, int regionX, int regionY) {
        for (int i = 0; i < 200 && sim.pendingGrassEventsForTest(regionX, regionY) > 0; i++) {
            GameConfig.get().maxCatchupGrassEventsPerFrame = 50_000;
            wakeRegionTight(sim, regionX, regionY);
        }
    }

    private static void updateActiveRegionsLikeGame(SimulationSystem sim, float playerX, float playerY, float zoomFactor) {
        float halfW = GameSettings.viewWidthPx(zoomFactor) / 2f;
        float halfH = GameSettings.viewHeightPx(zoomFactor) / 2f;
        float playerPxX = playerX * Constants.CELL_SIZE_PX;
        float playerPxY = playerY * Constants.CELL_SIZE_PX;
        sim.updateActiveRegions(
                playerPxX - halfW,
                playerPxY - halfH,
                playerPxX + halfW,
                playerPxY + halfH,
                playerX,
                playerY);
    }

    private static int viewportBottomCellY(float playerX, float playerY) {
        float halfHCells = GameSettings.viewHeightPx(1f) / Constants.CELL_SIZE_PX / 2f;
        return (int) Math.floor(playerY - halfHCells);
    }

    private static void runSimSeconds(SimulationSystem sim, World world, float playerX, float playerY, float seconds) {
        int ticks = Math.round(seconds * GameConfig.get().simTickHz);
        for (int i = 0; i < ticks; i++) {
            updateActiveRegionsLikeGame(sim, playerX, playerY, 1f);
            sim.tick();
        }
    }

    private static int countBareDirt(World world) {
        int n = 0;
        for (int x = 0; x < world.getWidth(); x++) {
            for (int y = 0; y < world.getHeight(); y++) {
                if (world.getGround(x, y) == BlockId.DIRT_GROUND && world.getFloor(x, y) == BlockId.AIR) {
                    n++;
                }
            }
        }
        return n;
    }

    private static int countBareDirtOnRow(World world, int rowY, float centerX) {
        int halfW = Constants.VIEW_WIDTH_CELLS / 2;
        int minX = Math.max(0, (int) centerX - halfW);
        int maxX = Math.min(world.getWidth() - 1, (int) centerX + halfW);
        int n = 0;
        for (int x = minX; x <= maxX; x++) {
            if (world.getGround(x, rowY) == BlockId.DIRT_GROUND && world.getFloor(x, rowY) == BlockId.AIR) {
                n++;
            }
        }
        return n;
    }
}
