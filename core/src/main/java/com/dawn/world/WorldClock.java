package com.dawn.world;

import com.dawn.config.DayNightConfig;

/** Saveable world time: normalized cycle position and day counter. */
public final class WorldClock {
    /** Default new-world start: 6:00 AM on the 24h display. */
    public static final float DEFAULT_START_TIME_OF_DAY = 5.5f / 24f;

    private float timeOfDay = DEFAULT_START_TIME_OF_DAY;
    private long dayIndex = 1L;

    public float timeOfDay() {
        return timeOfDay;
    }

    public long dayIndex() {
        return dayIndex;
    }

    public void setTimeOfDay(float value) {
        timeOfDay = wrapTimeOfDay(value);
    }

    public void setDayIndex(long value) {
        dayIndex = Math.max(1L, value);
    }

    /** Advances by real seconds; wraps at cycle end and increments {@link #dayIndex}. */
    public void advance(float deltaSeconds, DayNightConfig config) {
        if (deltaSeconds <= 0f) {
            return;
        }
        float cycle = config.cycleDurationSec();
        if (cycle <= 0f) {
            return;
        }
        float delta = deltaSeconds / cycle;
        timeOfDay += delta;
        while (timeOfDay >= 1f) {
            timeOfDay -= 1f;
            dayIndex++;
        }
    }

    /** Debug scrub: nudge cycle position without changing day index unless wrapping. */
    public void nudge(float deltaNormalized) {
        float next = timeOfDay + deltaNormalized;
        while (next >= 1f) {
            next -= 1f;
            dayIndex++;
        }
        while (next < 0f) {
            next += 1f;
            dayIndex = Math.max(1L, dayIndex - 1);
        }
        timeOfDay = next;
    }

    /** Maps {@link #timeOfDay} linearly to a 24-hour clock string {@code HH:MM}. */
    public String formatClock24h() {
        int totalMinutes = (int) Math.floor(timeOfDay * 24f * 60f);
        totalMinutes = Math.min(totalMinutes, 24 * 60 - 1);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private static float wrapTimeOfDay(float value) {
        float t = value % 1f;
        if (t < 0f) {
            t += 1f;
        }
        return t;
    }
}
