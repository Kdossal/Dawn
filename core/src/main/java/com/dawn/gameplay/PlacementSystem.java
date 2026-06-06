package com.dawn.gameplay;

import com.dawn.config.GameConfig;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemStack;
import com.dawn.entity.Entity;
import com.dawn.world.World;

/** Hold right-click to place dirt/sand at the cursor target (repeat interval). */
public final class PlacementSystem {
    private final InteractionSystem interaction;
    private float cooldown;

    public PlacementSystem(InteractionSystem interaction) {
        this.interaction = interaction;
    }

    public void update(
            World world,
            Entity entity,
            PlayerInventory inventory,
            TargetCell target,
            ItemStack held,
            boolean placeHeld,
            float delta) {
        if (!placeHeld) {
            cooldown = 0f;
            return;
        }
        if (target == null) {
            return;
        }

        cooldown -= delta;
        if (cooldown > 0f) {
            return;
        }

        if (interaction.tryPlace(
                world,
                entity,
                inventory,
                entity.getX(),
                entity.getY(),
                target.x(),
                target.y(),
                held)) {
            cooldown = GameConfig.get().placeRepeatIntervalSec;
        }
    }
}
