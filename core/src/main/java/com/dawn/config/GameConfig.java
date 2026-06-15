package com.dawn.config;

/** Tunable gameplay values (mutable singleton for testing). */
public final class GameConfig {
    /** Speed multiplier while running after a movement-key double-tap (50 = +50%). */
    public float runSpeedBonusPercent = 50f;
    /** Max seconds between two presses of the same movement key to start a run. */
    public float runDoubleTapWindowSec = 0.3f;
    public float reachDefault = 3f;
    /** Base weapon DPS added on top of wielder's melee bonus (tools, future weapons). */
    public float defaultToolWeaponDamage = 3f;
    public float placeRepeatIntervalSec = 0.12f;
    /** How long the interact pose lingers after a successful place. */
    public float placeInteractPulseSec = 0.12f;
    public float pickupRadiusCells = 1.5f;
    public float pickupCooldownSec = 1f;
    public float dropScatterCells = 0.25f;
    /** Seconds to hold RMB to finish eating. */
    public float eatDurationSec = 1.2f;
    /** Poison duration applied to the player at spawn. */
    public float poisonDurationSec = 15f;

    /** Max hunger/thirst pool (player vitals). */
    public float maxHunger = 100f;
    public float maxThirst = 100f;
    public float hpRegenPerSec = 0.5f;
    public float energyRegenPerSec = 5f;
    /** Energy spent per second while sprinting (double-tap run). */
    public float runEnergyDrainPerSec = 8f;
    public float hungerDrainPerSec = 0.1f;
    public float thirstDrainPerSec = 0.15f;

    public int simTickHz = 10;
    public int grassSpreadIntervalTicks = 15;
    /** Random spread attempts per grass spread event (each targets one adjacent dirt cell). */
    public int grassSpreadAttemptsPerEvent = 6;
    public int simMarginCells = 12;
    public int regionSizeCells = 16;
    /** Max tick-equivalents simulated when a region wakes (event-batched, not per-tick loop). */
    public int maxCatchupTicksPerChunk = 10_000;
    public int maxCatchupGrassEventsPerFrame = 3000;
    /** Seed for deterministic floor/ground decor variants (e.g. grass center tiles). */
    public int visualDecorSeed = 0xD4A4E;

    /** Day/night cycle (144 s = 2.4 min real time; scale cycle duration ×10 for 24-min days). */
    public float dayNightCycleDurationSec = 1440f;

    /** 24h clock fraction where sunrise begins (6:00 AM). */
    public float dayNightSunriseClockFraction = 6f / 24f;

    /** Phase shares: 46% night, 46% day, 2% sunrise, 2% sunset (4% transitions total). */
    public float dayNightNightShare = 0.46f;

    public float dayNightDayShare = 0.46f;
    public float dayNightSunriseShare = 0.04f;
    public float dayNightSunsetShare = 0.04f;
    /**
     * Global minimum light level: night ambient floor on the time-of-day curve and luma floor on
     * final per-tile tint. Color comes from chroma keyframes, not this scalar.
     */
    public float minLightLevel = 0.2f;

    /** Max block-light propagation distance and dirty-region halo (cells). */
    public int maxLightRadius = 32;

    /** Block light multiplied per cardinal propagation step. */
    public float lightCardinalFalloff = 0.9f;

    /** Block light multiplied per diagonal propagation step. */
    public float lightDiagonalFalloff = 0.85f;

    /** Propagation stops when strength drops below this threshold. */
    public float lightMinThreshold = 0.02f;

    private static final GameConfig INSTANCE = new GameConfig();

    public static GameConfig get() {
        return INSTANCE;
    }

    private GameConfig() {}
}
