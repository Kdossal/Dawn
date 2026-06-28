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
            ctx.equipmentSidebar.close();
            ctx.crateStorageOverlay.close();
            ctx.craftingOverlay.close();
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
                ctx.equipmentSidebar.setInventoryOverlayOpen(ctx.inventoryOverlay.isOpen());
                if (ctx.inventoryOverlay.isOpen()) {
                    ctx.crateStorageOverlay.close();
                }
                applyInputProcessor(ctx, hotbarInput);
            }
        }
        ctx.equipmentSidebar.setInventoryOverlayOpen(ctx.inventoryOverlay.isOpen());
    }

    void processHotbarAndScroll(GameContext ctx) {
        boolean lockSelection =
                ctx.inventoryOverlay.isOpen()
                        ? ctx.inventoryOverlay.locksHotbarSelection()
                        : ctx.equipmentSidebar.locksHotbarSelection();
        if (lockSelection) {
            int lockedIndex =
                    ctx.inventoryOverlay.isOpen()
                            ? ctx.inventoryOverlay.lockedHotbarIndex()
                            : ctx.equipmentSidebar.lockedHotbarIndex();
            ctx.inventory.setSelectedIndex(lockedIndex);
        }

        int prevSelected = ctx.inventory.getSelectedIndex();
        ctx.hotbar.update(lockSelection);
        if (ctx.inventoryOverlay.isOpen() && prevSelected != ctx.inventory.getSelectedIndex()) {
            ctx.inventoryOverlay.refreshAll();
        }

        float scrollY = ctx.input.consumeScrollY();
        if (scrollY != 0f && !ctx.inventoryOverlay.isOpen()) {
            ctx.hotbar.applyScroll(scrollY, lockSelection);
        }
    }

    void applyInputProcessor(GameContext ctx, InputAdapter hotbarInput) {
        if (ctx.pauseOverlay.isPaused()) {
            Gdx.input.setInputProcessor(ctx.pauseOverlay.stage());
        } else if (ctx.inventoryOverlay.isOpen()) {
            Gdx.input.setInputProcessor(new InputMultiplexer(ctx.inventoryOverlay.stage(), ctx.input));
        } else {
            Gdx.input.setInputProcessor(
                    new InputMultiplexer(ctx.equipmentSidebar.stage(), hotbarInput, ctx.input));
        }
    }
}
