package com.dawn.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.dawn.entity.Entity;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.light.LightEngine;

/** Handles simulation region updates and per-frame lighting rebuilds. */
final class SimulationLightingPhase {
    void tick(GameContext ctx, OrthographicCamera worldCamera, float delta) {
        updateSimulationRegions(ctx, worldCamera);
        ctx.gameLoop.update(delta);
        Entity player = ctx.entities.getPlayer();
        syncHeldLight(ctx, player, ctx.hotbar.getHeld(), ctx.equipment.get(EquipmentSlot.OFF_HAND));
        flushLighting(ctx);
    }

    private static void updateSimulationRegions(GameContext ctx, OrthographicCamera worldCamera) {
        Entity player = ctx.entities.getPlayer();
        float halfW = ctx.zoomController.viewWidthPx() / 2f;
        float halfH = ctx.zoomController.viewHeightPx() / 2f;
        ctx.gameLoop
                .getSimulation()
                .updateActiveRegions(
                        worldCamera.position.x - halfW,
                        worldCamera.position.y - halfH,
                        worldCamera.position.x + halfW,
                        worldCamera.position.y + halfH,
                        player.getX(),
                        player.getY());
    }

    private static void syncHeldLight(GameContext ctx, Entity player, ItemStack held, ItemStack offhand) {
        boolean lanternActive =
                (!held.isEmpty() && held.itemId == ItemId.LANTERN)
                        || (!offhand.isEmpty() && offhand.itemId == ItemId.LANTERN);
        if (lanternActive) {
            int cellX = (int) Math.floor(player.getX());
            int cellY = (int) Math.floor(player.getY());
            ctx.world.lightMap().updateHeldSource(
                    cellX,
                    cellY,
                    BlockDefinitions.lightEmission(BlockId.LANTERN),
                    BlockDefinitions.lightRadius(BlockId.LANTERN),
                    BlockDefinitions.lightColorR(BlockId.LANTERN),
                    BlockDefinitions.lightColorG(BlockId.LANTERN),
                    BlockDefinitions.lightColorB(BlockId.LANTERN),
                    true);
        } else {
            ctx.world.lightMap().updateHeldSource(0, 0, 0f, 0, 1f, 1f, 1f, false);
        }
    }

    private static void flushLighting(GameContext ctx) {
        if (ctx.world.lightMap().hasDirty()) {
            int[] bounds = ctx.world.lightMap().pollRebuildBounds();
            LightEngine.rebuild(ctx.world, bounds[0], bounds[1], bounds[2], bounds[3]);
        }
    }
}
