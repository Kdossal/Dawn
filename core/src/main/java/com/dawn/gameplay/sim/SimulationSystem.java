package com.dawn.gameplay.sim;

import com.dawn.config.Constants;
import com.dawn.config.GameConfig;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import java.util.Random;

/**
 * Region-based world sim (grass spread, bush spawn). Active regions tick live; waking regions catch up
 * via batched events to {@link #currentTick}.
 */
public class SimulationSystem {
    private static final int[][] SPREAD_DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final long REGION_RNG_SALT = 0x9E3779B97F4A7C15L;

    private static final class RegionState {
        final int regionX;
        final int regionY;
        long lastSimulatedTick;
        boolean active;
        int pendingGrassEvents;
        int pendingBushEvents;
        Random rng;

        RegionState(int regionX, int regionY) {
            this.regionX = regionX;
            this.regionY = regionY;
        }
    }

    private final World world;
    private final RegionState[][] regions;
    private final int regionsWide;
    private final int regionsHigh;
    private final int regionSize;
    private long currentTick;

    public SimulationSystem(World world) {
        this.world = world;
        GameConfig cfg = GameConfig.get();
        this.regionSize = cfg.regionSizeCells;
        this.regionsWide = (world.getWidth() + regionSize - 1) / regionSize;
        this.regionsHigh = (world.getHeight() + regionSize - 1) / regionSize;
        this.regions = new RegionState[regionsWide][regionsHigh];
        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                regions[rx][ry] = new RegionState(rx, ry);
            }
        }
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public int countActiveRegions() {
        int n = 0;
        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                if (regions[rx][ry].active) {
                    n++;
                }
            }
        }
        return n;
    }

    public int totalPendingGrassEvents() {
        int n = 0;
        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                n += regions[rx][ry].pendingGrassEvents;
            }
        }
        return n;
    }

    public int totalPendingBushEvents() {
        int n = 0;
        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                n += regions[rx][ry].pendingBushEvents;
            }
        }
        return n;
    }

    /** Whether this cell's region is sim-active this frame (grass/bush may write here). */
    public boolean isCellSimActive(int cellX, int cellY) {
        return isRegionActive(cellX, cellY);
    }

    public void updateActiveRegions(
            float cameraLeftPx,
            float cameraBottomPx,
            float cameraRightPx,
            float cameraTopPx,
            float playerX,
            float playerY) {
        GameConfig cfg = GameConfig.get();
        int margin = cfg.simMarginCells;

        int minCellX = (int) Math.floor(cameraLeftPx / Constants.CELL_SIZE_PX) - margin;
        int maxCellX = (int) Math.ceil(cameraRightPx / Constants.CELL_SIZE_PX) + margin;
        int minCellY = (int) Math.floor(cameraBottomPx / Constants.CELL_SIZE_PX) - margin;
        int maxCellY = (int) Math.ceil(cameraTopPx / Constants.CELL_SIZE_PX) + margin;

        int pCellX = (int) Math.floor(playerX);
        int pCellY = (int) Math.floor(playerY);
        minCellX = Math.min(minCellX, pCellX - margin);
        maxCellX = Math.max(maxCellX, pCellX + margin);
        minCellY = Math.min(minCellY, pCellY - margin);
        maxCellY = Math.max(maxCellY, pCellY + margin);

        int grassBudget = cfg.maxCatchupGrassEventsPerFrame;
        int bushBudget = cfg.maxCatchupBushEventsPerFrame;

        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                RegionState region = regions[rx][ry];
                boolean shouldBeActive = regionIntersects(rx, ry, minCellX, maxCellX, minCellY, maxCellY);
                if (shouldBeActive && !region.active) {
                    enqueueCatchUp(region);
                }
                region.active = shouldBeActive;
            }
        }

        FrameCatchUpBudget budget = new FrameCatchUpBudget(grassBudget, bushBudget);
        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                RegionState region = regions[rx][ry];
                if (!region.active) {
                    continue;
                }
                drainCatchUp(region, budget);
            }
        }
    }

    public void tick() {
        currentTick++;
        GameConfig cfg = GameConfig.get();

        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                RegionState region = regions[rx][ry];
                if (!region.active) {
                    continue;
                }
                if (region.pendingGrassEvents > 0 || region.pendingBushEvents > 0) {
                    continue;
                }
                region.lastSimulatedTick = currentTick;
                Random rng = regionRandom(region);
                if (currentTick % cfg.grassSpreadIntervalTicks == 0) {
                    tickGrassSpread(region, rng);
                }
                if (currentTick % cfg.bushSpawnIntervalTicks == 0) {
                    tickBushSpawn(region, rng);
                }
            }
        }
    }

    /** Package-private for tests. */
    void setRegionLastSimulatedTickForTest(int regionX, int regionY, long tick) {
        regions[regionX][regionY].lastSimulatedTick = tick;
    }

    long getRegionLastSimulatedTickForTest(int regionX, int regionY) {
        return regions[regionX][regionY].lastSimulatedTick;
    }

    int pendingGrassEventsForTest(int regionX, int regionY) {
        return regions[regionX][regionY].pendingGrassEvents;
    }

    int computeGrassCatchUpEventsForTest(long missedTicks) {
        return grassSpreadEventCount(
                effectiveMissedTicks(0, missedTicks, GameConfig.get().maxCatchupTicksPerChunk));
    }

    void advanceCurrentTickForTest(long delta) {
        currentTick += delta;
    }

    private void enqueueCatchUp(RegionState region) {
        long missed = currentTick - region.lastSimulatedTick;
        if (missed <= 0) {
            if (region.pendingGrassEvents == 0 && region.pendingBushEvents == 0) {
                region.lastSimulatedTick = currentTick;
            }
            return;
        }
        long effective =
                effectiveMissedTicks(region.lastSimulatedTick, currentTick, GameConfig.get().maxCatchupTicksPerChunk);
        int grass = grassSpreadEventCount(effective);
        int bush = bushSpawnEventCount(effective);
        // Keep the larger owed batch if a prior wake was interrupted (e.g. frame budget / deactivation).
        region.pendingGrassEvents = Math.max(region.pendingGrassEvents, grass);
        region.pendingBushEvents = Math.max(region.pendingBushEvents, bush);
    }

    private void drainCatchUp(RegionState region, FrameCatchUpBudget budget) {
        if (region.pendingGrassEvents <= 0 && region.pendingBushEvents <= 0) {
            return;
        }
        Random rng = regionRandom(region);
        int grassRun = Math.min(region.pendingGrassEvents, budget.grass);
        if (grassRun > 0) {
            runGrassSpreadEvents(region, rng, grassRun);
            region.pendingGrassEvents -= grassRun;
            budget.grass -= grassRun;
        }
        int bushRun = Math.min(region.pendingBushEvents, budget.bush);
        if (bushRun > 0) {
            runBushSpawnEvents(region, rng, bushRun);
            region.pendingBushEvents -= bushRun;
            budget.bush -= bushRun;
        }
        if (region.pendingGrassEvents <= 0 && region.pendingBushEvents <= 0) {
            region.lastSimulatedTick = currentTick;
        }
    }

    private static final class FrameCatchUpBudget {
        int grass;
        int bush;

        FrameCatchUpBudget(int grass, int bush) {
            this.grass = grass;
            this.bush = bush;
        }
    }

    static long effectiveMissedTicks(long fromTick, long toTick, int cap) {
        long missed = toTick - fromTick;
        if (missed <= 0) {
            return 0;
        }
        return Math.min(missed, cap);
    }

    private static int grassSpreadEventCount(long effectiveMissed) {
        if (effectiveMissed <= 0) {
            return 0;
        }
        return (int) (effectiveMissed / GameConfig.get().grassSpreadIntervalTicks);
    }

    private static int bushSpawnEventCount(long effectiveMissed) {
        if (effectiveMissed <= 0) {
            return 0;
        }
        return (int) (effectiveMissed / GameConfig.get().bushSpawnIntervalTicks);
    }

    private void runGrassSpreadEvents(RegionState region, Random rng, int eventCount) {
        for (int i = 0; i < eventCount; i++) {
            tickGrassSpread(region, rng);
        }
    }

    private void runBushSpawnEvents(RegionState region, Random rng, int eventCount) {
        GameConfig cfg = GameConfig.get();
        for (int i = 0; i < eventCount; i++) {
            tickBushSpawn(region, rng, cfg.bushSpawnChance);
        }
    }

    private Random regionRandom(RegionState region) {
        if (region.rng == null) {
            long seed = REGION_RNG_SALT ^ ((long) region.regionX << 32) ^ (region.regionY & 0xFFFFFFFFL);
            region.rng = new Random(seed);
        }
        return region.rng;
    }

    private boolean regionIntersects(int rx, int ry, int minCellX, int maxCellX, int minCellY, int maxCellY) {
        int rMinX = rx * regionSize;
        int rMaxX = rMinX + regionSize;
        int rMinY = ry * regionSize;
        int rMaxY = rMinY + regionSize;
        return rMaxX > minCellX && rMinX <= maxCellX && rMaxY > minCellY && rMinY <= maxCellY;
    }

    private boolean isRegionActive(int cellX, int cellY) {
        if (!world.inBounds(cellX, cellY)) {
            return false;
        }
        int rx = cellX / regionSize;
        int ry = cellY / regionSize;
        if (rx < 0 || ry < 0 || rx >= regionsWide || ry >= regionsHigh) {
            return false;
        }
        return regions[rx][ry].active;
    }

    private void tickGrassSpread(RegionState region, Random rng) {
        int startX = region.regionX * regionSize;
        int startY = region.regionY * regionSize;
        int endX = Math.min(startX + regionSize, world.getWidth());
        int endY = Math.min(startY + regionSize, world.getHeight());
        int width = Math.max(1, endX - startX);
        int height = Math.max(1, endY - startY);

        int attempts = GameConfig.get().grassSpreadAttemptsPerEvent;
        for (int i = 0; i < attempts; i++) {
            int x = startX + rng.nextInt(width);
            int y = startY + rng.nextInt(height);
            if (world.getFloor(x, y) != BlockId.GRASS) {
                continue;
            }
            int[] d = SPREAD_DIRS[rng.nextInt(SPREAD_DIRS.length)];
            int nx = x + d[0];
            int ny = y + d[1];
            if (!world.inBounds(nx, ny)) {
                continue;
            }
            if (!isRegionActive(nx, ny)) {
                continue;
            }
            if (world.getGround(nx, ny) == BlockId.DIRT
                    && world.getFloor(nx, ny) == BlockId.AIR
                    && world.getObject(nx, ny) == BlockId.AIR) {
                world.setFloor(nx, ny, BlockId.GRASS);
            }
        }
    }

    private void tickBushSpawn(RegionState region, Random rng) {
        tickBushSpawn(region, rng, GameConfig.get().bushSpawnChance);
    }

    private void tickBushSpawn(RegionState region, Random rng, float spawnChance) {
        int startX = region.regionX * regionSize;
        int startY = region.regionY * regionSize;
        int endX = Math.min(startX + regionSize, world.getWidth());
        int endY = Math.min(startY + regionSize, world.getHeight());
        int width = Math.max(1, endX - startX);
        int height = Math.max(1, endY - startY);

        int x = startX + rng.nextInt(width);
        int y = startY + rng.nextInt(height);
        if (!isRegionActive(x, y)) {
            return;
        }
        if (world.getFloor(x, y) != BlockId.GRASS || world.getObject(x, y) != BlockId.AIR) {
            return;
        }
        if (rng.nextFloat() < spawnChance) {
            world.setObject(x, y, BlockId.BUSH);
        }
    }
}
