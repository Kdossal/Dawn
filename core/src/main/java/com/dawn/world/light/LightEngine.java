package com.dawn.world.light;

import com.dawn.config.GameConfig;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Rebuilds block-light values via multi-source 8-direction multiplicative BFS. */
public final class LightEngine {
    private static final int[][] NEIGHBORS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private record LightNode(int x, int y, float strength) {}

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
        map.clearBlockLight(writeMinX, writeMinY, writeMaxX, writeMaxY);

        for (LightSource source : sources) {
            propagateFromSource(world, map, source, cfg, writeMinX, writeMinY, writeMaxX, writeMaxY);
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
            int writeMinX,
            int writeMinY,
            int writeMaxX,
            int writeMaxY) {
        ArrayDeque<LightNode> queue = new ArrayDeque<>();
        tryEnqueue(
                map,
                queue,
                source,
                cfg,
                source.x(),
                source.y(),
                source.emission(),
                writeMinX,
                writeMinY,
                writeMaxX,
                writeMaxY);

        while (!queue.isEmpty()) {
            LightNode node = queue.removeFirst();
            for (int[] dir : NEIGHBORS) {
                int dx = dir[0];
                int dy = dir[1];
                int nx = node.x() + dx;
                int ny = node.y() + dy;
                if (!world.inBounds(nx, ny)) {
                    continue;
                }
                boolean diagonal = dx != 0 && dy != 0;
                if (diagonal) {
                    if (BlockDefinitions.isLightBlockerAt(world, node.x() + dx, node.y())) {
                        continue;
                    }
                    if (BlockDefinitions.isLightBlockerAt(world, node.x(), node.y() + dy)) {
                        continue;
                    }
                }
                int chebyshev = Math.max(Math.abs(nx - source.x()), Math.abs(ny - source.y()));
                if (chebyshev > source.radius()) {
                    continue;
                }
                float factor = diagonal ? cfg.lightDiagonalFalloff : cfg.lightCardinalFalloff;
                float nextStrength = node.strength() * factor;
                if (nextStrength < cfg.lightMinThreshold) {
                    continue;
                }
                if (BlockDefinitions.isLightBlockerAt(world, nx, ny)) {
                    applyLightAt(
                            map,
                            source,
                            cfg,
                            nx,
                            ny,
                            nextStrength,
                            writeMinX,
                            writeMinY,
                            writeMaxX,
                            writeMaxY);
                    continue;
                }
                tryEnqueue(
                        map, queue, source, cfg, nx, ny, nextStrength, writeMinX, writeMinY, writeMaxX, writeMaxY);
            }
        }
    }

    /** Writes light onto a cell without enqueueing further propagation (occluder surface). */
    private static void applyLightAt(
            LightMap map,
            LightSource source,
            GameConfig cfg,
            int x,
            int y,
            float strength,
            int minX,
            int minY,
            int maxX,
            int maxY) {
        if (x < minX || y < minY || x > maxX || y > maxY) {
            return;
        }
        if (strength >= cfg.lightMinThreshold) {
            map.addColorContribution(x, y, strength, source.colorR(), source.colorG(), source.colorB());
        }
        if (map.sample(x, y) < strength) {
            map.setBlockLight(x, y, strength);
        }
    }

    private static boolean tryEnqueue(
            LightMap map,
            ArrayDeque<LightNode> queue,
            LightSource source,
            GameConfig cfg,
            int x,
            int y,
            float strength,
            int minX,
            int minY,
            int maxX,
            int maxY) {
        if (x < minX || y < minY || x > maxX || y > maxY) {
            return false;
        }
        if (strength >= cfg.lightMinThreshold) {
            map.addColorContribution(x, y, strength, source.colorR(), source.colorG(), source.colorB());
        }
        if (map.sample(x, y) >= strength) {
            return false;
        }
        map.setBlockLight(x, y, strength);
        queue.addLast(new LightNode(x, y, strength));
        return true;
    }
}
