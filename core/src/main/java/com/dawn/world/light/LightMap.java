package com.dawn.world.light;

import com.dawn.config.GameConfig;

/** Per-cell block light storage with region-based dirty tracking. */
public final class LightMap {
    private static final float[] NEUTRAL_COLOR = {1f, 1f, 1f};

    private final int width;
    private final int height;
    private final int regionSize;
    private final int regionsWide;
    private final int regionsHigh;
    private final int haloRegions;
    private final float[][] blockLight;
    private final float[][] colorSumR;
    private final float[][] colorSumG;
    private final float[][] colorSumB;
    private final float[][] colorSumWeight;
    private final boolean[][] regionDirty;
    private HeldLightSource heldSource;
    private int lastHeldCellX = -1;
    private int lastHeldCellY = -1;

    /** Mobile light from a held item (e.g. lantern in hotbar). */
    public record HeldLightSource(
            int cellX, int cellY, float emission, int radius, float colorR, float colorG, float colorB) {}

    public LightMap(int width, int height) {
        this.width = width;
        this.height = height;
        GameConfig cfg = GameConfig.get();
        this.regionSize = cfg.regionSizeCells;
        this.regionsWide = (width + regionSize - 1) / regionSize;
        this.regionsHigh = (height + regionSize - 1) / regionSize;
        this.haloRegions = Math.max(1, (cfg.maxLightRadius + regionSize - 1) / regionSize);
        this.blockLight = new float[width][height];
        this.colorSumR = new float[width][height];
        this.colorSumG = new float[width][height];
        this.colorSumB = new float[width][height];
        this.colorSumWeight = new float[width][height];
        this.regionDirty = new boolean[regionsWide][regionsHigh];
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public float sample(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return 0f;
        }
        return blockLight[x][y];
    }

    /** Weighted-average RGB of contributing sources; neutral white when unlit. */
    public float[] sampleColor(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return NEUTRAL_COLOR;
        }
        float weight = colorSumWeight[x][y];
        if (weight <= 0f) {
            return NEUTRAL_COLOR;
        }
        return new float[] {colorSumR[x][y] / weight, colorSumG[x][y] / weight, colorSumB[x][y] / weight};
    }

    void setBlockLight(int x, int y, float value) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            blockLight[x][y] = value;
        }
    }

    void addColorContribution(int x, int y, float strength, float colorR, float colorG, float colorB) {
        if (x < 0 || y < 0 || x >= width || y >= height || strength <= 0f) {
            return;
        }
        colorSumR[x][y] += strength * colorR;
        colorSumG[x][y] += strength * colorG;
        colorSumB[x][y] += strength * colorB;
        colorSumWeight[x][y] += strength;
    }

    void clearBlockLight(int minX, int minY, int maxX, int maxY) {
        int x0 = Math.max(0, minX);
        int y0 = Math.max(0, minY);
        int x1 = Math.min(width - 1, maxX);
        int y1 = Math.min(height - 1, maxY);
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                blockLight[x][y] = 0f;
                colorSumR[x][y] = 0f;
                colorSumG[x][y] = 0f;
                colorSumB[x][y] = 0f;
                colorSumWeight[x][y] = 0f;
            }
        }
    }

    public boolean hasDirty() {
        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                if (regionDirty[rx][ry]) {
                    return true;
                }
            }
        }
        return false;
    }

    public void markDirty(int cellX, int cellY) {
        if (cellX < 0 || cellY < 0 || cellX >= width || cellY >= height) {
            return;
        }
        int centerRx = cellX / regionSize;
        int centerRy = cellY / regionSize;
        for (int rx = centerRx - haloRegions; rx <= centerRx + haloRegions; rx++) {
            for (int ry = centerRy - haloRegions; ry <= centerRy + haloRegions; ry++) {
                if (rx >= 0 && ry >= 0 && rx < regionsWide && ry < regionsHigh) {
                    regionDirty[rx][ry] = true;
                }
            }
        }
    }

    public void markAllDirty() {
        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                regionDirty[rx][ry] = true;
            }
        }
    }

    public HeldLightSource heldSource() {
        return heldSource;
    }

    /** Marks affected regions dirty when the held source moves or toggles. */
    public void updateHeldSource(
            int cellX,
            int cellY,
            float emission,
            int radius,
            float colorR,
            float colorG,
            float colorB,
            boolean active) {
        if (active) {
            if (heldSource == null || cellX != lastHeldCellX || cellY != lastHeldCellY) {
                if (heldSource != null) {
                    markDirty(lastHeldCellX, lastHeldCellY);
                }
                markDirty(cellX, cellY);
                lastHeldCellX = cellX;
                lastHeldCellY = cellY;
            }
            heldSource = new HeldLightSource(cellX, cellY, emission, radius, colorR, colorG, colorB);
        } else if (heldSource != null) {
            markDirty(lastHeldCellX, lastHeldCellY);
            heldSource = null;
            lastHeldCellX = -1;
            lastHeldCellY = -1;
        }
    }

    /** Returns {@code [minX, minY, maxX, maxY]} in cell coords; clears dirty flags. */
    public int[] pollRebuildBounds() {
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        boolean any = false;
        for (int rx = 0; rx < regionsWide; rx++) {
            for (int ry = 0; ry < regionsHigh; ry++) {
                if (!regionDirty[rx][ry]) {
                    continue;
                }
                any = true;
                regionDirty[rx][ry] = false;
                int rMinX = rx * regionSize;
                int rMinY = ry * regionSize;
                int rMaxX = Math.min(width - 1, rMinX + regionSize - 1);
                int rMaxY = Math.min(height - 1, rMinY + regionSize - 1);
                minX = Math.min(minX, rMinX);
                minY = Math.min(minY, rMinY);
                maxX = Math.max(maxX, rMaxX);
                maxY = Math.max(maxY, rMaxY);
            }
        }
        if (!any) {
            return new int[] {0, 0, width - 1, height - 1};
        }
        int margin = GameConfig.get().maxLightRadius;
        return new int[] {
            Math.max(0, minX - margin),
            Math.max(0, minY - margin),
            Math.min(width - 1, maxX + margin),
            Math.min(height - 1, maxY + margin)
        };
    }
}
