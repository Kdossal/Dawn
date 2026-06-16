package com.dawn.world.light;

import com.dawn.config.GameConfig;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Rebuilds block-light values via multi-source corner-grid multiplicative BFS. */
public final class LightEngine {
    private static final int[][] NEIGHBORS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private record LightNode(int vx, int vy, float strength) {}

    private record LightSource(int x, int y, float emission, int radius, float colorR, float colorG, float colorB) {}

    private LightEngine() {}

    public static void rebuildFull(World world) {
        LightMap map = world.lightMap();
        map.markAllDirty();
        int[] bounds = map.pollRebuildBounds();
        rebuild(world, bounds[0], bounds[1], bounds[2], bounds[3]);
    }

    public static void rebuild(World world, int minX, int minY, int maxX, int maxY) {
        LightMap map = world.lightMap();
        GameConfig cfg = GameConfig.get();

        int margin = cfg.maxLightRadius;
        int searchMinX = Math.max(0, minX - margin);
        int searchMinY = Math.max(0, minY - margin);
        int searchMaxX = Math.min(world.getWidth() - 1, maxX + margin);
        int searchMaxY = Math.min(world.getHeight() - 1, maxY + margin);

        List<LightSource> sources = collectSources(world, searchMinX, searchMinY, searchMaxX, searchMaxY);
        LightMap.HeldLightSource held = map.heldSource();
        if (held != null) {
            int radius = Math.min(held.radius(), cfg.maxLightRadius);
            sources.add(
                    new LightSource(
                            held.cellX(),
                            held.cellY(),
                            held.emission(),
                            radius,
                            held.colorR(),
                            held.colorG(),
                            held.colorB()));
        }

        int writeMinX = minX;
        int writeMinY = minY;
        int writeMaxX = maxX;
        int writeMaxY = maxY;
        for (LightSource source : sources) {
            writeMinX = Math.min(writeMinX, source.x());
            writeMinY = Math.min(writeMinY, source.y());
            writeMaxX = Math.max(writeMaxX, source.x());
            writeMaxY = Math.max(writeMaxY, source.y());
        }
        int writeMinVx = writeMinX;
        int writeMinVy = writeMinY;
        int writeMaxVx = writeMaxX + 1;
        int writeMaxVy = writeMaxY + 1;
        map.clearCornerLight(writeMinVx, writeMinVy, writeMaxVx, writeMaxVy);

        for (LightSource source : sources) {
            propagateFromSource(
                    world,
                    map,
                    source,
                    cfg,
                    writeMinVx,
                    writeMinVy,
                    writeMaxVx,
                    writeMaxVy);
        }
    }

    private static List<LightSource> collectSources(
            World world, int minX, int minY, int maxX, int maxY) {
        List<LightSource> sources = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                BlockId object = world.getObject(x, y);
                float emission = BlockDefinitions.lightEmission(object);
                if (emission <= 0f) {
                    continue;
                }
                int radius = Math.min(BlockDefinitions.lightRadius(object), GameConfig.get().maxLightRadius);
                sources.add(
                        new LightSource(
                                x,
                                y,
                                emission,
                                radius,
                                BlockDefinitions.lightColorR(object),
                                BlockDefinitions.lightColorG(object),
                                BlockDefinitions.lightColorB(object)));
            }
        }
        return sources;
    }

    private static void propagateFromSource(
            World world,
            LightMap map,
            LightSource source,
            GameConfig cfg,
            int writeMinVx,
            int writeMinVy,
            int writeMaxVx,
            int writeMaxVy) {
        ArrayDeque<LightNode> queue = new ArrayDeque<>();
        tryEnqueue(map, queue, source, cfg, source.x(), source.y(), source.emission(), writeMinVx, writeMinVy, writeMaxVx, writeMaxVy);
        tryEnqueue(
                map,
                queue,
                source,
                cfg,
                source.x() + 1,
                source.y(),
                source.emission(),
                writeMinVx,
                writeMinVy,
                writeMaxVx,
                writeMaxVy);
        tryEnqueue(
                map,
                queue,
                source,
                cfg,
                source.x(),
                source.y() + 1,
                source.emission(),
                writeMinVx,
                writeMinVy,
                writeMaxVx,
                writeMaxVy);
        tryEnqueue(
                map,
                queue,
                source,
                cfg,
                source.x() + 1,
                source.y() + 1,
                source.emission(),
                writeMinVx,
                writeMinVy,
                writeMaxVx,
                writeMaxVy);

        while (!queue.isEmpty()) {
            LightNode node = queue.removeFirst();
            for (int[] dir : NEIGHBORS) {
                int dx = dir[0];
                int dy = dir[1];
                int nvx = node.vx() + dx;
                int nvy = node.vy() + dy;
                if (!inCornerBounds(world, nvx, nvy)) {
                    continue;
                }
                if (edgeBlocked(world, node.vx(), node.vy(), nvx, nvy)) {
                    continue;
                }
                int chebyshev = Math.max(Math.abs(nvx - source.x()), Math.abs(nvy - source.y()));
                if (chebyshev > source.radius()) {
                    continue;
                }
                boolean diagonal = dx != 0 && dy != 0;
                float factor = diagonal ? cfg.lightDiagonalFalloff : cfg.lightCardinalFalloff;
                float transmission = edgeTransmission(world, node.vx(), node.vy(), nvx, nvy);
                float nextStrength = node.strength() * factor * transmission;
                if (nextStrength < cfg.lightMinThreshold) {
                    continue;
                }
                tryEnqueue(
                        map,
                        queue,
                        source,
                        cfg,
                        nvx,
                        nvy,
                        nextStrength,
                        writeMinVx,
                        writeMinVy,
                        writeMaxVx,
                        writeMaxVy);
            }
        }
    }

    private static boolean tryEnqueue(
            LightMap map,
            ArrayDeque<LightNode> queue,
            LightSource source,
            GameConfig cfg,
            int vx,
            int vy,
            float strength,
            int minVx,
            int minVy,
            int maxVx,
            int maxVy) {
        if (vx < minVx || vy < minVy || vx > maxVx || vy > maxVy) {
            return false;
        }
        if (strength < cfg.lightMinThreshold) {
            return false;
        }
        boolean winnerChanged =
                map.applyCornerSample(vx, vy, strength, source.colorR(), source.colorG(), source.colorB());
        if (!winnerChanged) {
            return false;
        }
        queue.addLast(new LightNode(vx, vy, strength));
        return true;
    }

    private static boolean inCornerBounds(World world, int vx, int vy) {
        return vx >= 0 && vy >= 0 && vx <= world.getWidth() && vy <= world.getHeight();
    }

    private static boolean edgeBlocked(World world, int vx, int vy, int nvx, int nvy) {
        int dx = Integer.compare(nvx, vx);
        int dy = Integer.compare(nvy, vy);
        if (dx != 0 && dy != 0) {
            // The diagonal edge physically passes through the single cell whose lower-left
            // corner is the lower-left of the two vertices. A solid cell there blocks light
            // outright; without this check light leaks straight through enclosed walls.
            if (isBlockingCell(world, Math.min(vx, nvx), Math.min(vy, nvy))) {
                return true;
            }
            // Also prevent light from squeezing diagonally past a wall corner.
            return isBlockingCell(world, vx + dx, vy) || isBlockingCell(world, vx, vy + dy);
        }
        if (dx != 0) {
            int edgeX = Math.min(vx, nvx);
            int y = vy;
            // Traversing along a horizontal vertex edge should only be blocked when both
            // cells sharing that edge are blockers; otherwise light can graze along one side.
            return isBlockingCell(world, edgeX, y - 1) && isBlockingCell(world, edgeX, y);
        }
        int edgeY = Math.min(vy, nvy);
        int x = vx;
        // Traversing along a vertical vertex edge follows the same rule.
        return isBlockingCell(world, x - 1, edgeY) && isBlockingCell(world, x, edgeY);
    }

    private static boolean isBlockingCell(World world, int cellX, int cellY) {
        return world.inBounds(cellX, cellY) && BlockDefinitions.isLightBlockerAt(world, cellX, cellY);
    }

    /**
     * Returns the combined light transmission factor for crossing the edge from (vx,vy) to
     * (nvx,nvy). Fully opaque cells (transmission == 0) are excluded from this calculation
     * because their blocking role is already handled by {@link #edgeBlocked}; only partial
     * blockers contribute attenuation here.
     */
    private static float edgeTransmission(World world, int vx, int vy, int nvx, int nvy) {
        int dx = Integer.compare(nvx, vx);
        int dy = Integer.compare(nvy, vy);
        if (dx != 0 && dy != 0) {
            return partialTransmission(world, Math.min(vx, nvx), Math.min(vy, nvy));
        }
        if (dx != 0) {
            int edgeX = Math.min(vx, nvx);
            return Math.min(
                    partialTransmission(world, edgeX, vy - 1),
                    partialTransmission(world, edgeX, vy));
        }
        int edgeY = Math.min(vy, nvy);
        return Math.min(
                partialTransmission(world, vx - 1, edgeY),
                partialTransmission(world, vx, edgeY));
    }

    /**
     * Returns the cell's light transmission, treating fully opaque cells as 1.0 so that
     * grazing-along-wall paths (which are allowed by the cardinal {@code &&} blocking rule)
     * are not additionally penalised by the opaque cell they run beside.
     */
    private static float partialTransmission(World world, int cellX, int cellY) {
        if (!world.inBounds(cellX, cellY)) {
            return 1f;
        }
        float t = BlockDefinitions.lightTransmission(world.getObject(cellX, cellY));
        return t <= 0f ? 1f : t;
    }
}
