package com.dawn.render;

/** Level + normalized-chroma helpers for per-tile sprite tinting. */
public final class LightColor {

    private static final float[] WHITE_CHROMA = {1f, 1f, 1f};

    private LightColor() {}

    /** Scale RGB so the brightest channel is 1; returns white when input is zero. */
    public static float[] normalizeChroma(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        if (max <= 0f) {
            return WHITE_CHROMA.clone();
        }
        return new float[] {r / max, g / max, b / max};
    }

    public static float[] normalizeChroma(float[] rgb) {
        return normalizeChroma(rgb[0], rgb[1], rgb[2]);
    }

    /** Weighted blend of two normalized chromas; result is re-normalized. */
    public static float[] blendChroma(float[] a, float[] b, float weightB) {
        float w = clamp01(weightB);
        float inv = 1f - w;
        return normalizeChroma(
                a[0] * inv + b[0] * w,
                a[1] * inv + b[1] * w,
                a[2] * inv + b[2] * w);
    }

    public static float[] scale(float level, float[] chroma) {
        float l = clamp01(level);
        return new float[] {l * chroma[0], l * chroma[1], l * chroma[2]};
    }

    public static float maxLevel(float... levels) {
        float max = 0f;
        for (float level : levels) {
            max = Math.max(max, level);
        }
        return max;
    }

    public static float maxChannel(float[] rgb) {
        return Math.max(rgb[0], Math.max(rgb[1], rgb[2]));
    }

    /** Per-channel max so block tint never darkens below the ambient baseline. */
    public static float[] raiseChannelsToBaseline(float[] rgb, float[] baseline) {
        return new float[] {
            Math.max(rgb[0], baseline[0]),
            Math.max(rgb[1], baseline[1]),
            Math.max(rgb[2], baseline[2])
        };
    }

    /**
     * Luma-preserving floor: if max channel is below {@code floor}, scale RGB up uniformly.
     * Keeps hue when lifting very dark tiles.
     */
    public static float[] applyMinLightLevel(float[] rgb, float floor) {
        float f = clamp01(floor);
        float max = maxChannel(rgb);
        if (max <= 0f) {
            return new float[] {f, f, f};
        }
        if (max >= f) {
            return rgb.clone();
        }
        float scale = f / max;
        return new float[] {
            Math.min(1f, rgb[0] * scale),
            Math.min(1f, rgb[1] * scale),
            Math.min(1f, rgb[2] * scale)
        };
    }

    /** Per-channel shadow lift when enabled; {@code gamma} &lt; 1 raises low channels. */
    public static float[] applyDisplayGamma(float[] rgb, boolean enabled, float gamma) {
        if (!enabled || gamma <= 0f) {
            return rgb.clone();
        }
        return new float[] {
            (float) Math.pow(clamp01(rgb[0]), gamma),
            (float) Math.pow(clamp01(rgb[1]), gamma),
            (float) Math.pow(clamp01(rgb[2]), gamma)
        };
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
