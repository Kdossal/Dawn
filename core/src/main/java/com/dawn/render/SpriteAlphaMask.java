package com.dawn.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

/** Opaque pixel footprint sampled from PNG alpha (for pixel-accurate overlap tests). */
public final class SpriteAlphaMask {
    private static final int ALPHA_THRESHOLD = 128;

    private final int width;
    private final int height;
    private final boolean[] opaque;

    private SpriteAlphaMask(int width, int height, boolean[] opaque) {
        this.width = width;
        this.height = height;
        this.opaque = opaque;
    }

    public static SpriteAlphaMask loadInternal(String assetPath) {
        FileHandle file = Gdx.files.internal(assetPath);
        Pixmap pixmap = new Pixmap(file);
        try {
            return fromPixmap(pixmap);
        } finally {
            pixmap.dispose();
        }
    }

    public static SpriteAlphaMask fromPixmap(Pixmap pixmap) {
        return fromPixmapRegion(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight());
    }

    public static SpriteAlphaMask fromPixmapRegion(Pixmap pixmap, int originX, int originY, int width, int height) {
        boolean[] opaque = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = pixmap.getPixel(originX + x, originY + y) & 0xff;
                opaque[y * width + x] = alpha >= ALPHA_THRESHOLD;
            }
        }
        return new SpriteAlphaMask(width, height, opaque);
    }

    /** Test-only / synthetic mask builder. */
    public static SpriteAlphaMask of(int width, int height, boolean[] opaqueRowMajor) {
        if (opaqueRowMajor.length != width * height) {
            throw new IllegalArgumentException("opaque length must be width * height");
        }
        return new SpriteAlphaMask(width, height, opaqueRowMajor.clone());
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean isOpaque(int localX, int localY) {
        if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
            return false;
        }
        return opaque[localY * width + localX];
    }

    /**
     * True when any world pixel is opaque in both masks (LibGDX bottom-left sprite origins).
     */
    public static boolean opaqueOverlap(
            SpriteAlphaMask a,
            float aOriginX,
            float aOriginY,
            SpriteAlphaMask b,
            float bOriginX,
            float bOriginY) {
        if (a == null || b == null) {
            return false;
        }

        int aLeft = Math.round(aOriginX);
        int aBottom = Math.round(aOriginY);
        int aRight = aLeft + a.width;
        int aTop = aBottom + a.height;

        int bLeft = Math.round(bOriginX);
        int bBottom = Math.round(bOriginY);
        int bRight = bLeft + b.width;
        int bTop = bBottom + b.height;

        int ix0 = Math.max(aLeft, bLeft);
        int iy0 = Math.max(aBottom, bBottom);
        int ix1 = Math.min(aRight, bRight);
        int iy1 = Math.min(aTop, bTop);
        if (ix0 >= ix1 || iy0 >= iy1) {
            return false;
        }

        for (int wy = iy0; wy < iy1; wy++) {
            for (int wx = ix0; wx < ix1; wx++) {
                if (a.isOpaque(wx - aLeft, wy - aBottom) && b.isOpaque(wx - bLeft, wy - bBottom)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Like {@link #opaqueOverlap} but samples player mask with horizontal flip. */
    public static boolean opaqueOverlapFlippedX(
            SpriteAlphaMask a,
            float aOriginX,
            float aOriginY,
            SpriteAlphaMask b,
            float bOriginX,
            float bOriginY) {
        if (a == null || b == null) {
            return false;
        }

        int aLeft = Math.round(aOriginX);
        int aBottom = Math.round(aOriginY);
        int aRight = aLeft + a.width;
        int aTop = aBottom + a.height;

        int bLeft = Math.round(bOriginX);
        int bBottom = Math.round(bOriginY);
        int bRight = bLeft + b.width;
        int bTop = bBottom + b.height;

        int ix0 = Math.max(aLeft, bLeft);
        int iy0 = Math.max(aBottom, bBottom);
        int ix1 = Math.min(aRight, bRight);
        int iy1 = Math.min(aTop, bTop);
        if (ix0 >= ix1 || iy0 >= iy1) {
            return false;
        }

        for (int wy = iy0; wy < iy1; wy++) {
            for (int wx = ix0; wx < ix1; wx++) {
                int aLocalX = a.width - 1 - (wx - aLeft);
                if (a.isOpaque(aLocalX, wy - aBottom) && b.isOpaque(wx - bLeft, wy - bBottom)) {
                    return true;
                }
            }
        }
        return false;
    }
}
