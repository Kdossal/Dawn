package com.dawn.config;

/** Tunable day/night cycle: clock-anchored phases and real-time duration. */
public final class DayNightConfig {
    /** 24h clock fraction where sunrise begins (6:00 AM = 0.25). */
    public float sunriseClockFraction = 6f / 24f;

    /** Phase shares of one cycle (normalized to sum 1.0 at runtime). */
    public float nightShare = 0.46f;

    public float dayShare = 0.46f;
    public float sunriseShare = 0.04f;
    public float sunsetShare = 0.04f;

    /** Real seconds for a full cycle (default 144 s = 2.4 min). Scale ×10 for 24-min days. */
    public float cycleDurationSec = 144f;

    /** Night ambient level floor; copied from {@link GameConfig#minLightLevel}. */
    public float minLightLevel = 0.12f;

    public float cycleDurationSec() {
        return cycleDurationSec;
    }

    public float nightFraction() {
        return nightShare / phaseShareSum();
    }

    public float dayFraction() {
        return dayShare / phaseShareSum();
    }

    public float sunriseFraction() {
        return sunriseShare / phaseShareSum();
    }

    public float sunsetFraction() {
        return sunsetShare / phaseShareSum();
    }

    /** Normalized [0,1) start of sunrise (6 AM by default). */
    public float sunriseStart() {
        return wrapFraction(sunriseClockFraction);
    }

    public float dayStart() {
        return wrapFraction(sunriseStart() + sunriseFraction());
    }

    public float sunsetStart() {
        return wrapFraction(dayStart() + dayFraction());
    }

    /** Night begins after sunset; may wrap past midnight. */
    public float nightStart() {
        return wrapFraction(sunsetStart() + sunsetFraction());
    }

    public float sunriseSec() {
        return cycleDurationSec * sunriseFraction();
    }

    public float daySec() {
        return cycleDurationSec * dayFraction();
    }

    public float sunsetSec() {
        return cycleDurationSec * sunsetFraction();
    }

    public float nightSec() {
        return cycleDurationSec * nightFraction();
    }

    private float phaseShareSum() {
        return nightShare + dayShare + sunriseShare + sunsetShare;
    }

    private static float wrapFraction(float value) {
        float t = value % 1f;
        if (t < 0f) {
            t += 1f;
        }
        return t;
    }

    public static DayNightConfig from(GameConfig cfg) {
        DayNightConfig d = new DayNightConfig();
        d.sunriseClockFraction = cfg.dayNightSunriseClockFraction;
        d.nightShare = cfg.dayNightNightShare;
        d.dayShare = cfg.dayNightDayShare;
        d.sunriseShare = cfg.dayNightSunriseShare;
        d.sunsetShare = cfg.dayNightSunsetShare;
        d.cycleDurationSec = cfg.dayNightCycleDurationSec;
        d.minLightLevel = cfg.minLightLevel;
        return d;
    }
}
