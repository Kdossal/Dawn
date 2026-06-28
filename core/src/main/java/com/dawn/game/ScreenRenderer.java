package com.dawn.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.dawn.config.DayNightConfig;
import com.dawn.config.GameConfig;
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
            long simTick,
            boolean showInteractHint,
            boolean playerMoving) {
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
            renderHud(ctx, hudViewport, hudCamera, target, player, showInteractHint);
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
        boolean dayNight = ctx.renderSettings.dayNightEnabled;
        boolean gammaEnabled = ctx.renderSettings.displayGammaEnabled;
        float gamma = ctx.renderSettings.displayGamma;
        ctx.worldRenderer.renderTerrain(
                ctx.world,
                bounds[0],
                bounds[1],
                bounds[2],
                bounds[3],
                ctx.world.clock().timeOfDay(),
                DayNightConfig.from(GameConfig.get()),
                ctx.renderSettings.localLightingEnabled,
                dayNight,
                gammaEnabled,
                gamma,
                alignX,
                alignY);
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
                ctx.world.clock().timeOfDay(),
                DayNightConfig.from(GameConfig.get()),
                ctx.renderSettings.localLightingEnabled,
                dayNight,
                gammaEnabled,
                gamma,
                alignX,
                alignY);
        if (!paused) {
            if (ctx.interactionPresentation.showPlacementGhosts()) {
                ctx.worldRenderer.renderPlacementGhosts(
                        ctx.world, ctx.interactionPresentation.placementPreviews(), alignX, alignY);
            }
            ctx.worldRenderer.renderInteractionHighlights(
                    ctx.world, ctx.interactionPresentation.breakHighlights(), alignX, alignY);
        }
        ctx.worldBatch.end();

        ctx.worldOverlay.setProjectionMatrix(worldCamera.combined);
        if (!paused && ctx.debug.isWorldDebugVisible()) {
            com.dawn.entity.EntityBounds entityBounds = player.bounds(ctx.assets);
            ItemStack held = ctx.equipmentSidebar.interactionHeld(ctx.hotbar.getHeld());
            float reach = ReachResolver.radiusCellsFloatForHeld(held);
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
            Entity player,
            boolean showInteractHint) {
        hudViewport.apply(hudCamera);
        hudCamera.update();
        ctx.hud.setProjection(hudCamera.combined);
        if (!ctx.inventoryOverlay.isOpen()) {
            ItemStack held = ctx.equipmentSidebar.interactionHeld(ctx.hotbar.getHeld());
            boolean cursorGrabbed = ctx.equipmentSidebar.hasHeldCursor();
            boolean craftPlacement = ctx.craftingSystem.isPlacementMode();
            ItemStack hintHeld = craftPlacement ? ctx.craftingSystem.phantomHeld() : held;
            boolean craftHoverValid =
                    craftPlacement
                            && ctx.craftingSystem.hasValidPlacementPreview(ctx.world, player, target);
            boolean suppressLeftHints = cursorGrabbed || ctx.craftingSystem.isPlaceChanneling();
            ctx.vitalsHud.render(player);
            ctx.statusHud.render(player);
            ClickHintRenderer.render(
                    ctx.hud,
                    ctx.assets,
                    ClickHintResolver.resolve(
                            ctx.world,
                            player,
                            hintHeld,
                            target,
                            suppressLeftHints,
                            craftPlacement,
                            craftHoverValid),
                    !cursorGrabbed && !ctx.hotbar.getHeld().isEmpty(),
                    true,
                    !showInteractHint,
                    showInteractHint);
            ctx.equipmentSidebar.draw();
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
        ItemStack held = ctx.equipmentSidebar.interactionHeld(ctx.hotbar.getHeld());
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
                ctx.profile,
                ctx.inventory,
                ctx.equipment,
                moveX,
                moveY,
                delta,
                interactionMessage,
                simTick,
                hoverSimActive,
                Gdx.input.getX(),
                Gdx.input.getY(),
                ctx.world,
                hoverBreak);
    }
}
