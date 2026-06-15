package com.dawn.entity;

import com.dawn.entity.status.StatusModifiers;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.InventoryWeight;
import com.dawn.inventory.PlayerInventory;

/** Snapshot of attributes, vitals, and derived stats for F3 debug. */
public record CharacterSheet(
        int vitality,
        int brawn,
        int agility,
        int focus,
        int intellect,
        int arcana,
        float currentHp,
        float maxHp,
        float currentEnergy,
        float maxEnergy,
        float currentHunger,
        float maxHunger,
        float currentThirst,
        float maxThirst,
        float armor,
        float currentWeight,
        int maxWeight,
        float moveSpeed,
        float dodgePercent,
        float hpRegenPerSec,
        float energyRegenPerSec,
        float hungerDrainPerSec,
        float thirstDrainPerSec) {

    public static CharacterSheet from(
            Entity entity, PlayerInventory inventory, EquipmentInventory equipment) {
        Stats stats = entity.getStats();
        float carried = InventoryWeight.totalWeight(inventory, equipment);
        float speedMult = StatusModifiers.moveSpeedMultiplier(entity.getStatuses());
        float dodgeMult = StatusModifiers.dodgeMultiplier(entity.getStatuses());
        return new CharacterSheet(
                stats.get(AttributeId.VITALITY),
                stats.get(AttributeId.BRAWN),
                stats.get(AttributeId.AGILITY),
                stats.get(AttributeId.FOCUS),
                stats.get(AttributeId.INTELLECT),
                stats.get(AttributeId.ARCANA),
                entity.getCurrentHp(),
                entity.getMaxHp(),
                entity.getCurrentEnergy(),
                entity.getMaxEnergy(),
                entity.getCurrentHunger(),
                entity.getMaxHunger(),
                entity.getCurrentThirst(),
                entity.getMaxThirst(),
                StatFormulas.armor(stats),
                carried,
                StatFormulas.maxCarryWeight(stats),
                StatFormulas.moveSpeedCellsPerSec(stats, false) * speedMult,
                StatFormulas.dodgePercent(stats) * dodgeMult,
                StatusModifiers.effectiveHpRegenPerSec(
                        StatFormulas.hpRegenPerSec(), entity.getStatuses()),
                StatusModifiers.effectiveEnergyRegenPerSec(
                        StatFormulas.energyRegenPerSec(), entity.getStatuses()),
                StatFormulas.hungerDrainPerSec(),
                StatFormulas.thirstDrainPerSec());
    }
}
