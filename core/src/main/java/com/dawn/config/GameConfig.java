package com.dawn.config;

/** Tunable gameplay values (mutable singleton for testing). */
public final class GameConfig {
    public float baseMoveSpeed = 6f;
    public float agilitySpeedFactor = 0.03f;
    /** Speed multiplier while running after a movement-key double-tap (50 = +50%). */
    public float runSpeedBonusPercent = 50f;
    /** Max seconds between two presses of the same movement key to start a run. */
    public float runDoubleTapWindowSec = 0.3f;
    public float baseMaxHealth = 0f;
    public float vigorHealthFactor = 8f;
    public float strengthArmorFactor = 0.5f;
    public float reachDefault = 3f;
    public float baseMiningDamagePerSec = 14f;
    public float handToolPowerPercent = 18f;
    public float placeRepeatIntervalSec = 0.12f;
    public float pickupRadiusCells = 1.5f;
    public float pickupCooldownSec = 1f;
    public float dropScatterCells = 0.25f;
    public int simTickHz = 10;
    public int grassSpreadIntervalTicks = 15;
    /** Random spread attempts per grass spread event (each targets one adjacent dirt cell). */
    public int grassSpreadAttemptsPerEvent = 6;
    public int bushSpawnIntervalTicks = 30;
    public float bushSpawnChance = 0.35f;
    public int simMarginCells = 12;
    public int regionSizeCells = 16;
    /** Max tick-equivalents simulated when a region wakes (event-batched, not per-tick loop). */
    public int maxCatchupTicksPerChunk = 10_000;
    public int maxCatchupGrassEventsPerFrame = 3000;
    public int maxCatchupBushEventsPerFrame = 1500;
    /** Seed for deterministic floor/ground decor variants (e.g. grass center tiles). */
    public int visualDecorSeed = 0xD4A4E;

    private static final GameConfig INSTANCE = new GameConfig();

    public static GameConfig get() {
        return INSTANCE;
    }

    private GameConfig() {}
}
