package com.dawn.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;

/** Handles pause/inventory mode transitions and input routing. */
final class UiModePhase {
    void processModeToggles(GameContext ctx, FrameState frame, InputAdapter hotbarInput) {
        boolean wasPaused = ctx.pauseOverlay.isPaused();
        ctx.pauseOverlay.handleToggleKey();
        frame.paused = ctx.pauseOverlay.isPaused();
        if (!wasPaused && frame.paused) {
            ctx.inventoryOverlay.close();
            ctx.mining.reset();
            ctx.interactionPresentation.clear();
            applyInputProcessor(ctx, hotbarInput);
        } else if (wasPaused && !frame.paused) {
            applyInputProcessor(ctx, hotbarInput);
        }

        if (!frame.paused) {
            boolean wasOpen = ctx.inventoryOverlay.isOpen();
            ctx.inventoryOverlay.handleToggleKey();
            if (wasOpen != ctx.inventoryOverlay.isOpen()) {
                applyInputProcessor(ctx, hotbarInput);
            }
        }
    }

    void processHotbarAndScroll(GameContext ctx) {
        int prevActiveRow = ctx.inventory.getActiveRow();
        ctx.hotbar.update();
        if (ctx.inventoryOverlay.isOpen() && prevActiveRow != ctx.inventory.getActiveRow()) {
            ctx.inventoryOverlay.refreshAll();
        }

        float scrollY = ctx.input.consumeScrollY();
        if (scrollY != 0f && !ctx.inventoryOverlay.isOpen()) {
            ctx.hotbar.applyScroll(scrollY);
        }
    }

    void applyInputProcessor(GameContext ctx, InputAdapter hotbarInput) {
        if (ctx.pauseOverlay.isPaused()) {
            Gdx.input.setInputProcessor(ctx.pauseOverlay.stage());
        } else if (ctx.inventoryOverlay.isOpen()) {
            Gdx.input.setInputProcessor(new InputMultiplexer(ctx.inventoryOverlay.stage(), ctx.input));
        } else {
            Gdx.input.setInputProcessor(new InputMultiplexer(hotbarInput, ctx.input));
        }
    }
}
