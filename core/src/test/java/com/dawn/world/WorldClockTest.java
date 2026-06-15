package com.dawn.world;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.config.DayNightConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorldClockTest {
    private WorldClock clock;
    private DayNightConfig config;

    @BeforeEach
    void setUp() {
        clock = new WorldClock();
        config = new DayNightConfig();
    }

    @Test
    void advance_zeroDelta_doesNothing() {
        clock.setTimeOfDay(0.25f);
        clock.setDayIndex(2L);
        clock.advance(0f, config);
        assertEquals(0.25f, clock.timeOfDay(), 1e-5f);
        assertEquals(2L, clock.dayIndex());
    }

    @Test
    void advance_fullCycle_incrementsDayIndex() {
        clock.setTimeOfDay(0.9f);
        clock.setDayIndex(1L);
        clock.advance(config.cycleDurationSec() * 0.2f, config);
        assertTrue(clock.timeOfDay() < 0.2f);
        assertEquals(2L, clock.dayIndex());
    }

    @Test
    void defaultStartsAtSixAm() {
        assertEquals("05:30", clock.formatClock24h());
        assertEquals(WorldClock.DEFAULT_START_TIME_OF_DAY, clock.timeOfDay(), 1e-5f);
    }

    @Test
    void advance_respectsCycleDuration() {
        clock.setTimeOfDay(0f);
        clock.advance(config.cycleDurationSec(), config);
        assertEquals(0f, clock.timeOfDay(), 1e-5f);
        assertEquals(2L, clock.dayIndex());
    }

    @Test
    void formatClock24h_mapsLinearly() {
        clock.setTimeOfDay(0f);
        assertEquals("00:00", clock.formatClock24h());
        clock.setTimeOfDay(0.25f);
        assertEquals("06:00", clock.formatClock24h());
        clock.setTimeOfDay(0.5f);
        assertEquals("12:00", clock.formatClock24h());
    }

    @Test
    void nudge_positiveWrapsDayIndex() {
        clock.setTimeOfDay(0.99f);
        clock.setDayIndex(3L);
        clock.nudge(0.02f);
        assertTrue(clock.timeOfDay() < 0.05f);
        assertEquals(4L, clock.dayIndex());
    }
}
