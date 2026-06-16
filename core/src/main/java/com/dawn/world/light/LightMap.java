package com.dawn.world.light;

import com.dawn.config.GameConfig;

/** Corner-grid block light storage with region-based dirty tracking. */
public final class LightMap {
    private static final float[] NEUTRAL_COLOR = {1f, 1f, 1f};
    private static final float LIGHT_EPSILON = 1e-4f;

    private final int width;
    private final int height;
    private final int regionSize;
    private final int regionsWide;
    private final int regionsHigh;
    private final int haloRegions;
    private final float[][] cornerLight;
    private final float[][] cornerColorSumR;
    private final float[][] cornerColorSumG;
    private final float[][] cornerColorSumB;
    private final float[][] cornerColorSumWeight;
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
        this.cornerLight = new float[width + 1][height + 1];
        this.cornerColorSumR = new float[width + 1][height + 1];
        this.cornerColorSumG = new float[width + 1][height + 1];
        this.cornerColorSumB = new float[width + 1][height + 1];
        this.cornerColorSumWeight = new float[width + 1][height + 1];
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
        float bl = sampleCorner(x, y);
        float br = sampleCorner(x + 1, y);
        float tl = sampleCorner(x, y + 1);
        float tr = sampleCorner(x + 1, y + 1);
        return Math.max(Math.max(bl, br), Math.max(tl, tr));
    }

    /** Weighted-average RGB of contributing sources; neutral white when unlit. */
    public float[] sampleColor(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return NEUTRAL_COLOR;
        }
        final float epsilon = 1e-4f;
        float[] bl = sampleCornerColor(x, y);
        float[] br = sampleCornerColor(x + 1, y);
        float[] tl = sampleCornerColor(x, y + 1);
        float[] tr = sampleCornerColor(x + 1, y + 1);

        float wBl = sampleCorner(x, y);
        float wBr = sampleCorner(x + 1, y);
        float wTl = sampleCorner(x, y + 1);
        float wTr = sampleCorner(x + 1, y + 1);
        float max = Math.max(Math.max(wBl, wBr), Math.max(wTl, wTr));
        if (max <= 1e-6f) {
            return NEUTRAL_COLOR;
        }
        float r = 0f;
        float g = 0f;
        float b = 0f;
        int contributors = 0;

        if (Math.abs(wBl - max) <= epsilon) {
            r += bl[0];
            g += bl[1];
            b += bl[2];
            contributors++;
        }
        if (Math.abs(wBr - max) <= epsilon) {
            r += br[0];
            g += br[1];
            b += br[2];
            contributors++;
        }
        if (Math.abs(wTl - max) <= epsilon) {
            r += tl[0];
            g += tl[1];
            b += tl[2];
            contributors++;
        }
        if (Math.abs(wTr - max) <= epsilon) {
            r += tr[0];
            g += tr[1];
            b += tr[2];
            contributors++;
        }
        if (contributors == 0) {
            return NEUTRAL_COLOR;
        }
        return new float[] {r / contributors, g / contributors, b / contributors};
    }

    public float sampleCorner(int vx, int vy) {
        if (vx < 0 || vy < 0 || vx > width || vy > height) {
            return 0f;
        }
        return cornerLight[vx][vy];
    }

    /** Weighted-average RGB at a corner vertex; neutral white when unlit. */
    public float[] sampleCornerColor(int vx, int vy) {
        if (vx < 0 || vy < 0 || vx > width || vy > height) {
            return NEUTRAL_COLOR;
        }
        float weight = cornerColorSumWeight[vx][vy];
        if (weight <= 0f) {
            return NEUTRAL_COLOR;
        }
        return new float[] {
            cornerColorSumR[vx][vy] / weight,
            cornerColorSumG[vx][vy] / weight,
            cornerColorSumB[vx][vy] / weight
        };
    }

    void setCornerLight(int vx, int vy, float value) {
        if (vx >= 0 && vy >= 0 && vx <= width && vy <= height) {
            cornerLight[vx][vy] = value;
        }
    }

    void addCornerColorContribution(int vx, int vy, float strength, float colorR, float colorG, float colorB) {
        if (vx < 0 || vy < 0 || vx > width || vy > height || strength <= 0f) {
            return;
        }
        cornerColorSumR[vx][vy] += strength * colorR;
        cornerColorSumG[vx][vy] += strength * colorG;
        cornerColorSumB[vx][vy] += strength * colorB;
        cornerColorSumWeight[vx][vy] += strength;
    }

    /**
     * Applies a corner light sample using winner-consistent semantics.
     *
     * <p>- Stronger sample replaces corner level and resets corner chroma to the new winner.
     *
     * <p>- Equal-strength sample blends chroma as a tie.
     *
     * <p>- Weaker sample is ignored.
     *
     * @return true when level winner changed and propagation should continue from this corner.
     */
    boolean applyCornerSample(int vx, int vy, float strength, float colorR, float colorG, float colorB) {
        if (vx < 0 || vy < 0 || vx > width || vy > height || strength <= 0f) {
            return false;
        }
        float existing = cornerLight[vx][vy];
        if (strength < existing - LIGHT_EPSILON) {
            return false;
        }
        if (strength > existing + LIGHT_EPSILON) {
            cornerLight[vx][vy] = strength;
            cornerColorSumR[vx][vy] = strength * colorR;
            cornerColorSumG[vx][vy] = strength * colorG;
            cornerColorSumB[vx][vy] = strength * colorB;
            cornerColorSumWeight[vx][vy] = strength;
            return true;
        }
        addCornerColorContribution(vx, vy, strength, colorR, colorG, colorB);
        return false;
    }

    void clearCornerLight(int minVx, int minVy, int maxVx, int maxVy) {
        int x0 = Math.max(0, minVx);
        int y0 = Math.max(0, minVy);
        int x1 = Math.min(width, maxVx);
        int y1 = Math.min(height, maxVy);
        for (int vx = x0; vx <= x1; vx++) {
            for (int vy = y0; vy <= y1; vy++) {
                cornerLight[vx][vy] = 0f;
                cornerColorSumR[vx][vy] = 0f;
                cornerColorSumG[vx][vy] = 0f;
                cornerColorSumB[vx][vy] = 0f;
                cornerColorSumWeight[vx][vy] = 0f;
            }
        }
    }

    /** Backward-compatible clear method accepting cell bounds. */
    void clearBlockLight(int minX, int minY, int maxX, int maxY) {
        clearCornerLight(minX, minY, maxX + 1, maxY + 1);
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
            boolean moved = heldSource == null || cellX != lastHeldCellX || cellY != lastHeldCellY;
            boolean paramsChanged =
                    heldSource != null
                            && (Math.abs(heldSource.emission() - emission) > 1e-6f
                                    || heldSource.radius() != radius
                                    || Math.abs(heldSource.colorR() - colorR) > 1e-6f
                                    || Math.abs(heldSource.colorG() - colorG) > 1e-6f
                                    || Math.abs(heldSource.colorB() - colorB) > 1e-6f);
            if (moved || paramsChanged) {
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
