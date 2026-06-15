package com.dawn.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.config.GameConfig;
import com.dawn.entity.status.StatusSystem;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VitalSystemTest {
    private float savedHpRegen;
    private float savedEnergyRegen;
    private float savedHungerDrain;
    private float savedThirstDrain;
    private float savedRunDrain;

    @BeforeEach
    void saveConfig() {
        GameConfig cfg = GameConfig.get();
        savedHpRegen = cfg.hpRegenPerSec;
        savedEnergyRegen = cfg.energyRegenPerSec;
        savedHungerDrain = cfg.hungerDrainPerSec;
        savedThirstDrain = cfg.thirstDrainPerSec;
        savedRunDrain = cfg.runEnergyDrainPerSec;
        cfg.hpRegenPerSec = 10f;
        cfg.energyRegenPerSec = 10f;
        cfg.hungerDrainPerSec = 10f;
        cfg.thirstDrainPerSec = 10f;
        cfg.runEnergyDrainPerSec = 12f;
    }

    @AfterEach
    void restoreConfig() {
        GameConfig cfg = GameConfig.get();
        cfg.hpRegenPerSec = savedHpRegen;
        cfg.energyRegenPerSec = savedEnergyRegen;
        cfg.hungerDrainPerSec = savedHungerDrain;
        cfg.thirstDrainPerSec = savedThirstDrain;
        cfg.runEnergyDrainPerSec = savedRunDrain;
    }

    @Test
    void update_regensHpAndEnergy_drainsHungerAndThirst() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.setPoisoned(false);
        player.adjustVitals(-10f, -10f, player.getMaxHunger() - player.getCurrentHunger(), 0f);
        StatusSystem.refresh(player, new PlayerInventory(), new EquipmentInventory());

        VitalSystem.update(player, 1f);

        assertEquals(player.getMaxHp() - 10f + 10f, player.getCurrentHp(), 0.001f);
        assertEquals(player.getMaxEnergy() - 10f + 10f, player.getCurrentEnergy(), 0.001f);
        assertEquals(player.getMaxHunger() - 10f, player.getCurrentHunger(), 0.001f);
        assertEquals(player.getMaxThirst() - 10f, player.getCurrentThirst(), 0.001f);
    }

    @Test
    void update_whileRunning_drainsEnergyWithoutRegen() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        float start = player.getCurrentEnergy();

        VitalSystem.update(player, 1f, true);

        assertEquals(start - 12f, player.getCurrentEnergy(), 0.001f);
    }

    @Test
    void update_poisonedPlayer_drainsHp() {
        GameConfig.get().hpRegenPerSec = 0.5f;
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.adjustVitals(-5f, 0f, 0f, 0f);
        float hpBefore = player.getCurrentHp();
        StatusSystem.refresh(player, new PlayerInventory(), new EquipmentInventory());

        VitalSystem.update(player, 1f);

        assertEquals(hpBefore - 0.5f, player.getCurrentHp(), 0.001f);
    }

    @Test
    void update_clampsAtBounds() {
        Entity player = new Entity(EntityId.PLAYER, 50f, 50f);
        player.adjustVitals(-player.getMaxHp(), -player.getMaxEnergy(), -player.getMaxHunger(), -player.getMaxThirst());

        VitalSystem.update(player, 100f);

        assertTrue(player.getCurrentHp() <= player.getMaxHp());
        assertTrue(player.getCurrentEnergy() <= player.getMaxEnergy());
        assertEquals(0f, player.getCurrentHunger(), 0.001f);
        assertEquals(0f, player.getCurrentThirst(), 0.001f);
    }
}
