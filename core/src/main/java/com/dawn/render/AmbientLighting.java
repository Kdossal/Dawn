package com.dawn.render;

import com.dawn.config.DayNightConfig;
import com.dawn.world.World;

/**
 * World ambient lighting: time-of-day level and normalized chroma, combined in {@link TileLighting}.
 *
 * <p><b>ambientLevel</b> — scalar brightness {@code [minLightLevel..1]} (no hue).
 * <p><b>ambientChroma</b> — normalized phase tint (cool blue at night, orange/purple at transitions).
 */
public final class AmbientLighting {
    private static final float[] DAY_CHROMA = {1f, 1f, 1f};
    private static final float[] NIGHT_CHROMA = {0.55f, 0.65f, 1f};
    private static final float[] PURPLE_CHROMA = {0.72f, 0.48f, 0.95f};
    private static final float[] ORANGE_CHROMA = {1f, 0.82f, 0.68f};

    private static final float SUNSET_ORANGE_AT = 0.05f;
    private static final float SUNSET_PURPLE_AT = 0.3f;

    private AmbientLighting() {}

    public enum Phase {
        SUNRISE,
        DAY,
        SUNSET,
        NIGHT
    }

    public record PhaseInfo(Phase phase, float phaseT) {}

    private record ChromaKeyframe(float t, float[] rgb) {}

    public static PhaseInfo resolvePhase(float timeOfDay, DayNightConfig config) {
        float t = wrapTimeOfDay(timeOfDay);
        float sunrise = config.sunriseStart();
        float day = config.dayStart();
        float sunset = config.sunsetStart();
        float night = config.nightStart();

        if (inRange(t, sunrise, day)) {
            return new PhaseInfo(Phase.SUNRISE, (t - sunrise) / span(sunrise, day));
        }
        if (inRange(t, day, sunset)) {
            return new PhaseInfo(Phase.DAY, (t - day) / span(day, sunset));
        }
        if (inRange(t, sunset, night)) {
            return new PhaseInfo(Phase.SUNSET, (t - sunset) / span(sunset, night));
        }
        return new PhaseInfo(Phase.NIGHT, phaseTInNight(t, night, sunrise));
    }

    /**
     * Outdoor sun level factor at a cell; always 1 until indoor zones supply a zone min level hook.
     */
    public static float sunFactorAt(int cellX, int cellY, World world) {
        return 1f;
    }

    /**
     * Continuous ambient level {@code [minLightLevel..1]} driven by phase position.
     * Brightness only — chroma comes from {@link #ambientChroma}.
     */
    public static float ambientLevel(float timeOfDay, DayNightConfig config) {
        PhaseInfo info = resolvePhase(timeOfDay, config);
        float nightFloor = config.minLightLevel;
        return switch (info.phase()) {
            case DAY -> 1.0f;
            case NIGHT -> nightFloor;
            case SUNRISE -> lerp(nightFloor, 1.0f, smoothstep(info.phaseT()));
            case SUNSET -> lerp(1.0f, nightFloor, smoothstep(info.phaseT()));
        };
    }

    /** Normalized phase chroma; day is neutral white. */
    public static float[] ambientChroma(float timeOfDay, DayNightConfig config) {
        PhaseInfo info = resolvePhase(timeOfDay, config);
        float[] raw =
                switch (info.phase()) {
                    case SUNRISE -> chromaSunrise(info.phaseT());
                    case DAY -> DAY_CHROMA;
                    case SUNSET -> chromaSunset(info.phaseT());
                    case NIGHT -> NIGHT_CHROMA;
                };
        return LightColor.normalizeChroma(raw);
    }

    public static String phaseLabel(float timeOfDay, DayNightConfig config) {
        return switch (resolvePhase(timeOfDay, config).phase()) {
            case SUNRISE -> "Sunrise";
            case DAY -> "Day";
            case SUNSET -> "Sunset";
            case NIGHT -> "Night";
        };
    }

    private static float[] chromaSunrise(float phaseT) {
        return interpolateChromaKeyframes(
                phaseT,
                chromaKeyframe(0f, NIGHT_CHROMA),
                chromaKeyframe(0.5f, PURPLE_CHROMA),
                chromaKeyframe(0.8f, ORANGE_CHROMA),
                chromaKeyframe(1f, DAY_CHROMA));
    }

    private static float[] chromaSunset(float phaseT) {
        return interpolateChromaKeyframes(
                phaseT,
                chromaKeyframe(0f, DAY_CHROMA),
                chromaKeyframe(SUNSET_ORANGE_AT, ORANGE_CHROMA),
                chromaKeyframe(SUNSET_PURPLE_AT, PURPLE_CHROMA),
                chromaKeyframe(1f, NIGHT_CHROMA));
    }

    private static ChromaKeyframe chromaKeyframe(float t, float[] rgb) {
        return new ChromaKeyframe(t, rgb);
    }

    private static float[] interpolateChromaKeyframes(float phaseT, ChromaKeyframe... keyframes) {
        float t = clamp01(phaseT);
        if (t <= keyframes[0].t()) {
            return LightColor.normalizeChroma(keyframes[0].rgb());
        }
        for (int i = 1; i < keyframes.length; i++) {
            ChromaKeyframe end = keyframes[i];
            ChromaKeyframe start = keyframes[i - 1];
            if (t <= end.t()) {
                float u = smoothstep((t - start.t()) / (end.t() - start.t()));
                float[] blended =
                        new float[] {
                            lerp(start.rgb()[0], end.rgb()[0], u),
                            lerp(start.rgb()[1], end.rgb()[1], u),
                            lerp(start.rgb()[2], end.rgb()[2], u)
                        };
                return LightColor.normalizeChroma(blended);
            }
        }
        return LightColor.normalizeChroma(keyframes[keyframes.length - 1].rgb());
    }

    private static float phaseTInNight(float t, float nightStart, float sunriseStart) {
        float nightSpan = span(nightStart, sunriseStart);
        float offset = t >= nightStart ? t - nightStart : t + (1f - nightStart);
        return offset / nightSpan;
    }

    private static float span(float start, float end) {
        if (start <= end) {
            return end - start;
        }
        return (1f - start) + end;
    }

    private static boolean inRange(float t, float start, float end) {
        if (start <= end) {
            return t >= start && t < end;
        }
        return t >= start || t < end;
    }

    private static float smoothstep(float t) {
        float u = clamp01(t);
        return u * u * (3f - 2f * u);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp01(t);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static float wrapTimeOfDay(float value) {
        float t = value % 1f;
        if (t < 0f) {
            t += 1f;
        }
        return t;
    }
}
