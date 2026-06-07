package com.dawn.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.config.GameConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatFormulasTest {
    private float savedBase;
    private float savedRunBonus;

    @BeforeEach
    void saveConfig() {
        GameConfig cfg = GameConfig.get();
        savedBase = cfg.baseMoveSpeed;
        savedRunBonus = cfg.runSpeedBonusPercent;
        cfg.baseMoveSpeed = 6f;
        cfg.runSpeedBonusPercent = 50f;
    }

    @AfterEach
    void restoreConfig() {
        GameConfig cfg = GameConfig.get();
        cfg.baseMoveSpeed = savedBase;
        cfg.runSpeedBonusPercent = savedRunBonus;
    }

    @Test
    void runAppliesBonusPercent() {
        Stats stats = Stats.withUniformBase(10);
        assertEquals(6f, StatFormulas.moveSpeedCellsPerSec(stats), 0.001f);
        assertEquals(9f, StatFormulas.moveSpeedCellsPerSec(stats, true), 0.001f);
    }
}
