package com.dawn.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.test.GameConfigTestSupport;
import com.dawn.test.TestStats;
import org.junit.jupiter.api.Test;

class StatFormulasTest {
    @Test
    void moveSpeed_atAgility10_isFive() {
        Stats stats = TestStats.uniform(10);
        assertEquals(5f, StatFormulas.moveSpeedCellsPerSec(stats), 0.001f);
    }

    @Test
    void moveSpeed_atAgility20_isEight() {
        Stats stats = TestStats.withBase(10, 10, 20, 10, 10, 10);
        assertEquals(8f, StatFormulas.moveSpeedCellsPerSec(stats), 0.001f);
    }

    @Test
    void runAppliesBonusPercent() {
        GameConfigTestSupport.withRunSpeedBonus(50f, () -> {
            Stats stats = TestStats.uniform(10);
            assertEquals(7.5f, StatFormulas.moveSpeedCellsPerSec(stats, true), 0.001f);
        });
    }

    @Test
    void maxHp_vitality10Brawn10_is25() {
        Stats stats = TestStats.uniform(10);
        assertEquals(25f, StatFormulas.maxHealth(stats), 0.001f);
    }

    @Test
    void maxEnergy_vitality10_is25() {
        Stats stats = TestStats.uniform(10);
        assertEquals(25f, StatFormulas.maxEnergy(stats), 0.001f);
    }

    @Test
    void armor_vitality10_is2() {
        Stats stats = TestStats.uniform(10);
        assertEquals(2f, StatFormulas.armor(stats), 0.001f);
    }

    @Test
    void maxCarryWeight_brawn10_is20() {
        Stats stats = TestStats.uniform(10);
        assertEquals(20, StatFormulas.maxCarryWeight(stats));
    }

    @Test
    void dodgePercent_agility10_isFive() {
        Stats stats = TestStats.uniform(10);
        assertEquals(5f, StatFormulas.dodgePercent(stats), 0.001f);
    }

    @Test
    void meleeBonus_brawn10_isFive() {
        Stats stats = TestStats.uniform(10);
        assertEquals(5f, StatFormulas.meleeBonus(stats), 0.001f);
    }

    @Test
    void grabDamagePerSec_focus10_isTwo() {
        Stats stats = TestStats.uniform(10);
        assertEquals(2f, StatFormulas.grabDamagePerSec(stats), 0.001f);
    }

    @Test
    void grabDamagePerSec_focus14_isThree() {
        Stats stats = TestStats.withBase(10, 10, 10, 14, 10, 10);
        assertEquals(3f, StatFormulas.grabDamagePerSec(stats), 0.001f);
    }
}
