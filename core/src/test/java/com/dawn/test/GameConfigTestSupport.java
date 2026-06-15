package com.dawn.test;

import com.dawn.config.GameConfig;
import java.util.function.Consumer;

/** Save/restore {@link GameConfig} fields for isolated tests. */
public final class GameConfigTestSupport {
    private GameConfigTestSupport() {}

    public static void withRunSpeedBonus(float bonus, Runnable test) {
        float saved = GameConfig.get().runSpeedBonusPercent;
        try {
            GameConfig.get().runSpeedBonusPercent = bonus;
            test.run();
        } finally {
            GameConfig.get().runSpeedBonusPercent = saved;
        }
    }

    public static void withEatDuration(float seconds, Runnable test) {
        float saved = GameConfig.get().eatDurationSec;
        try {
            GameConfig.get().eatDurationSec = seconds;
            test.run();
        } finally {
            GameConfig.get().eatDurationSec = saved;
        }
    }

    public static void withConfig(Consumer<GameConfig> mutate, Runnable test) {
        GameConfig cfg = GameConfig.get();
        float runBonus = cfg.runSpeedBonusPercent;
        float eatDuration = cfg.eatDurationSec;
        float poisonDuration = cfg.poisonDurationSec;
        float placeRepeat = cfg.placeRepeatIntervalSec;
        try {
            mutate.accept(cfg);
            test.run();
        } finally {
            cfg.runSpeedBonusPercent = runBonus;
            cfg.eatDurationSec = eatDuration;
            cfg.poisonDurationSec = poisonDuration;
            cfg.placeRepeatIntervalSec = placeRepeat;
        }
    }
}
