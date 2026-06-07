package com.dawn.gameplay;

import com.dawn.config.GameConfig;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.entity.Entity;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.Layer;

/** Hold-to-mine: damage persists per cell in {@link com.dawn.world.BlockDamageStore}. */
public final class MiningSystem {
    private final InteractionSystem interaction;

    private int targetX;
    private int targetY;
    private Layer targetLayer;
    private boolean active;

    public MiningSystem(InteractionSystem interaction) {
        this.interaction = interaction;
    }

    public void update(
            World world,
            Entity entity,
            TargetResolver.TargetCell target,
            ItemStack held,
            boolean miningHeld,
            float delta) {
        if (!miningHeld || target == null) {
            active = false;
            return;
        }

        float reach = ReachResolver.radiusForHeld(held);
        if (!InteractionRules.canTargetCell(world, entity, entity.getX(), entity.getY(), target.x(), target.y(), reach)) {
            active = false;
            return;
        }

        BreakTarget breakTarget =
                InteractionRules.resolveToolBreak(world, held, target.x(), target.y(), entity);
        if (breakTarget == null) {
            BreakTarget raw = InteractionRules.inspectBreak(world, target.x(), target.y());
            if (raw != null) {
                BlockDefinitions.BlockDef def = BlockDefinitions.get(raw.blockId());
                if (def != null) {
                    String toolErr = InteractionRules.toolError(held, def.breakTags());
                    if (toolErr != null) {
                        interaction.setMessage(toolErr);
                    }
                }
            }
            active = false;
            return;
        }

        if (!active || targetX != breakTarget.x() || targetY != breakTarget.y()) {
            targetX = breakTarget.x();
            targetY = breakTarget.y();
            targetLayer = breakTarget.layer();
            active = true;
        }

        float maxHealth = breakTarget.health();
        float remaining =
                world.getBlockDamage().getRemaining(targetLayer, targetX, targetY, maxHealth);

        float dps = GameConfig.get().baseMiningDamagePerSec * (miningPowerPercent(held) / 100f);
        remaining -= dps * delta;
        world.getBlockDamage().setRemaining(targetLayer, targetX, targetY, remaining, maxHealth);

        if (remaining <= 0f) {
            interaction.executeBreak(
                    world, breakTarget.x(), breakTarget.y(), breakTarget.layer(), breakTarget.blockId());
            world.getBlockDamage().clear(targetLayer, targetX, targetY);
            active = false;
        }
    }

    private static float miningPowerPercent(ItemStack held) {
        GameConfig cfg = GameConfig.get();
        if (held == null || held.isEmpty()) {
            return cfg.handToolPowerPercent;
        }
        ItemDef def = ItemRegistry.get(held);
        if (def == null) {
            return cfg.handToolPowerPercent;
        }
        if (def.toolPowerPercent() > 0) {
            return def.toolPowerPercent();
        }
        return cfg.handToolPowerPercent;
    }

    public boolean isActive() {
        return active;
    }

    public void reset() {
        active = false;
    }
}
