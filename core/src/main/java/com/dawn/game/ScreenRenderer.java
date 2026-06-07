package com.dawn.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.dawn.render.PixelAlign;
import com.dawn.gameplay.ClickHintResolver;
import com.dawn.gameplay.InteractionRules;
import com.dawn.gameplay.ReachResolver;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.ui.ClickHintRenderer;
import com.dawn.input.InputController;
import com.dawn.entity.Entity;
import com.dawn.item.ItemStack;
import com.dawn.render.GameViewport;
import com.dawn.render.HudViewport;
import com.dawn.world.render.WorldRenderer;

/** Draws one frame: world, HUD, and debug layers. */
public final class ScreenRenderer {
    public void render(
            GameContext ctx,
            GameViewport gameViewport,
            HudViewport hudViewport,
            OrthographicCamera worldCamera,
            OrthographicCamera hudCamera,
            TargetCell target,
            Vector3 mouseWorld,
            InputController input,
            Entity player,
            float moveX,
            float moveY,
            float delta,
            String interactionMessage,
            long simTick) {
        int[] bounds = WorldRenderer.visibleCellBounds(
                worldCamera.position.x,
                worldCamera.position.y,
                ctx.zoomController.viewWidthPx(),
                ctx.zoomController.viewHeightPx(),
                ctx.world.getWidth(),
                ctx.world.getHeight());

        boolean paused = ctx.pauseOverlay.isPaused();
        renderWorld(ctx, gameViewport, worldCamera, bounds, paused);
        if (paused) {
            renderPause(ctx, hudViewport, hudCamera);
        } else {
            renderHud(ctx, hudViewport, hudCamera, target, player);
            renderDebug(
                    ctx,
                    hudViewport,
                    hudCamera,
                    input,
                    player,
                    target,
                    moveX,
                    moveY,
                    delta,
                    interactionMessage,
                    simTick);
        }
    }

    private void renderPause(GameContext ctx, HudViewport hudViewport, OrthographicCamera hudCamera) {
        hudViewport.apply(hudCamera);
        hudCamera.update();
        ctx.pauseOverlay.draw();
    }

    private void renderWorld(
            GameContext ctx,
            GameViewport gameViewport,
            OrthographicCamera worldCamera,
            int[] bounds,
            boolean paused) {
        gameViewport.apply(worldCamera);
        worldCamera.update();
        ctx.worldBatch.setProjectionMatrix(worldCamera.combined);
        ctx.worldBatch.begin();
        float alignX = PixelAlign.gridOffset(worldCamera.position.x);
        float alignY = PixelAlign.gridOffset(worldCamera.position.y);
        ctx.worldRenderer.renderTerrain(
                ctx.world, bounds[0], bounds[1], bounds[2], bounds[3], alignX, alignY);
        Entity player = ctx.entities.getPlayer();
        ctx.worldRenderer.renderSortedWorld(
                ctx.world,
                bounds[0],
                bounds[1],
                bounds[2],
                bounds[3],
                player.getX(),
                player.getY(),
                player.resolveSpriteFrame(ctx.assets),
                player.bounds(ctx.assets),
                ctx.renderSettings.occlusionFadeEnabled,
                ctx.dropSystem.getDrops(),
                alignX,
                alignY);
        if (!paused) {
            if (ctx.interactionPresentation.showPlacementGhosts()) {
                ctx.worldRenderer.renderPlacementGhosts(
                        ctx.interactionPresentation.placementPreviews(), alignX, alignY);
            }
            ctx.worldRenderer.renderInteractionHighlights(
                    ctx.world, ctx.interactionPresentation.breakHighlights(), alignX, alignY);
        }
        ctx.worldBatch.end();

        ctx.worldOverlay.setProjectionMatrix(worldCamera.combined);
        if (!paused && ctx.debug.isVisible()) {
            com.dawn.entity.EntityBounds entityBounds = player.bounds(ctx.assets);
            float reach = ReachResolver.radiusForHeld(ctx.hotbar.getHeld());
            ctx.worldRenderer.renderReachRing(entityBounds.moveCenterX(), entityBounds.moveCenterY(), reach);
            ctx.worldRenderer.renderEntityCollisionDebug(
                    ctx.world,
                    entityBounds,
                    new Color(0.2f, 1f, 0.35f, 1f),
                    new Color(1f, 0.9f, 0.2f, 1f));
        }
    }

    private void renderHud(
            GameContext ctx,
            HudViewport hudViewport,
            OrthographicCamera hudCamera,
            TargetCell target,
            Entity player) {
        hudViewport.apply(hudCamera);
        hudCamera.update();
        ctx.hud.setProjection(hudCamera.combined);
        if (!ctx.inventoryOverlay.isOpen()) {
            ctx.hotbar.render();
            ClickHintRenderer.render(
                    ctx.hud,
                    ctx.assets,
                    ClickHintResolver.resolve(ctx.world, player, ctx.hotbar.getHeld(), target));
        }
        ctx.inventoryOverlay.draw();
    }

    private void renderDebug(
            GameContext ctx,
            HudViewport hudViewport,
            OrthographicCamera hudCamera,
            InputController input,
            Entity player,
            TargetCell target,
            float moveX,
            float moveY,
            float delta,
            String interactionMessage,
            long simTick) {
        hudViewport.apply(hudCamera);
        hudCamera.update();
        ctx.hud.setProjection(hudCamera.combined);
        ItemStack held = ctx.hotbar.getHeld();
        com.dawn.gameplay.BreakTarget hoverBreak =
                target == null
                        ? null
                        : InteractionRules.resolveToolBreak(
                                ctx.world, held, target.x(), target.y(), player);
        com.dawn.gameplay.sim.SimulationSystem simulation = ctx.gameLoop.getSimulation();
        Boolean hoverSimActive =
                target == null ? null : simulation.isCellSimActive(target.x(), target.y());
        ctx.debug.render(
                input,
                player,
                ctx.inventory,
                moveX,
                moveY,
                delta,
                interactionMessage,
                simTick,
                simulation.countActiveRegions(),
                simulation.totalPendingGrassEvents(),
                simulation.totalPendingBushEvents(),
                hoverSimActive,
                Gdx.input.getX(),
                Gdx.input.getY(),
                ctx.world,
                hoverBreak);
    }
}
