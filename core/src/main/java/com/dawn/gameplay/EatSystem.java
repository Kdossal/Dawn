package com.dawn.gameplay;

import com.dawn.config.GameConfig;
import com.dawn.entity.Entity;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import java.util.function.IntFunction;

/** Hold right-click to eat edible held items (interact pose while channeling). */
public final class EatSystem {
    private float eatProgressSec;
    private float interactPulseRemaining;

    public void tick(float delta) {
        if (interactPulseRemaining > 0f) {
            interactPulseRemaining = Math.max(0f, interactPulseRemaining - delta);
        }
    }

    public void update(
            Entity player,
            PlayerInventory inventory,
            ItemStack held,
            boolean eatHeld,
            float delta,
            IntFunction<ItemStack> extractor) {
        if (!eatHeld || !canEat(player, held)) {
            eatProgressSec = 0f;
            return;
        }

        eatProgressSec += delta;
        if (eatProgressSec >= GameConfig.get().eatDurationSec) {
            completeEat(player, inventory, held, extractor);
            eatProgressSec = 0f;
            interactPulseRemaining = GameConfig.get().placeInteractPulseSec;
        }
    }

    public void update(
            Entity player, PlayerInventory inventory, ItemStack held, boolean eatHeld, float delta) {
        update(player, inventory, held, eatHeld, delta, null);
    }

    public boolean isEating() {
        return eatProgressSec > 0f;
    }

    public boolean isInteracting() {
        return isEating() || interactPulseRemaining > 0f;
    }

    public static boolean canEat(Entity player, ItemStack held) {
        if (player == null || held == null || held.isEmpty()) {
            return false;
        }
        ItemDef def = ItemRegistry.get(held);
        if (def == null || !def.isEdible()) {
            return false;
        }
        return player.getCurrentHunger() < player.getMaxHunger();
    }

    private static void completeEat(
            Entity player,
            PlayerInventory inventory,
            ItemStack held,
            IntFunction<ItemStack> extractor) {
        ItemDef def = ItemRegistry.get(held);
        if (def == null || !def.isEdible()) {
            return;
        }
        ItemStack extracted =
                extractor != null ? extractor.apply(1) : inventory.extractFromHeld(1);
        if (extracted == null || extracted.isEmpty()) {
            return;
        }
        HungerRestore.apply(player, def.eatHungerRestore());
    }
}
