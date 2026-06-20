package com.dawn.game;

import com.dawn.entity.Entity;
import com.dawn.entity.VitalSystem;
import com.dawn.entity.sprite.PlayerAnimContext;
import com.dawn.entity.status.StatusId;
import com.dawn.entity.status.StatusSystem;
import com.dawn.gameplay.EatSystem;
import com.dawn.item.ItemStack;

/** Handles player-state progression and world interaction updates for a frame. */
final class PlayerAndInteractionPhase {
    void tickPlayer(GameContext ctx, FrameState frame, float delta) {
        Entity player = ctx.entities.getPlayer();
        player.tickEffects(delta);
        StatusSystem.refresh(player, ctx.inventory, ctx.equipment);
        frame.lastMoveX = ctx.input.getMoveX();
        frame.lastMoveY = ctx.input.getMoveY();
        boolean running = ctx.input.isRunningWithEnergy(player.getCurrentEnergy());
        boolean immobile = player.getStatuses().has(StatusId.IMMOBILE);
        if (immobile) {
            ctx.input.cancelRun();
        }
        if (!immobile && (frame.lastMoveX != 0f || frame.lastMoveY != 0f)) {
            normalizeMove(frame);
            float speed = player.getMoveSpeedCellsPerSec(running) * delta;
            player.move(frame.lastMoveX * speed, frame.lastMoveY * speed, ctx.world);
        }

        VitalSystem.update(player, delta, running);
        if (player.getCurrentEnergy() <= 0f) {
            ctx.input.cancelRun();
        }
    }

    void tickInteraction(GameContext ctx, FrameState frame, float delta) {
        Entity player = ctx.entities.getPlayer();
        ctx.placement.tick(delta);
        ctx.eat.tick(delta);

        frame.inventoryOpen = ctx.inventoryOverlay.isOpen();
        if (frame.inventoryOpen) {
            ctx.mining.reset();
            ctx.interactionPresentation.clear();
            player.updateAnimation(delta, PlayerAnimContext.idle(player.getX(), player.getY()));
            ctx.inventoryOverlay.act(delta);
            ctx.dropSystem.update(delta);
            ctx.dropSystem.tryPickupAll(player, ctx.inventory);
            return;
        }

        frame.overHotbar = isOverHotbar(ctx);
        if (!frame.overHotbar) {
            ctx.mining.update(
                    ctx.world,
                    player,
                    frame.target,
                    ctx.hotbar.getHeld(),
                    ctx.input.miningHeld(),
                    delta);

            ItemStack held = ctx.hotbar.getHeld();
            if (EatSystem.canEat(player, held)) {
                ctx.eat.update(player, ctx.inventory, held, ctx.input.placeHeld(), delta);
            } else {
                ctx.placement.update(
                        ctx.world,
                        player,
                        ctx.inventory,
                        frame.target,
                        held,
                        ctx.input.placeHeld(),
                        delta);
            }
        } else {
            ctx.mining.reset();
        }

        if (ctx.input.dropFullStackPressed()) {
            ctx.dropSystem.dropFromEntity(player, ctx.inventory, true);
        } else if (ctx.input.dropPressed()) {
            ctx.dropSystem.dropFromEntity(player, ctx.inventory, false);
        }

        ctx.dropSystem.update(delta);
        ctx.dropSystem.tryPickupAll(player, ctx.inventory);

        ctx.interactionPresentation.update(
                ctx.world,
                player,
                ctx.hotbar.getHeld(),
                frame.target,
                !ctx.input.placeHeld());

        frame.interacting = isInteracting(ctx, frame.overHotbar);
        frame.moving = isMoving(frame.lastMoveX, frame.lastMoveY);
        player.updateAnimation(delta, buildAnimContext(frame, player));
    }

    private static void normalizeMove(FrameState frame) {
        float len = (float) Math.sqrt(frame.lastMoveX * frame.lastMoveX + frame.lastMoveY * frame.lastMoveY);
        frame.lastMoveX /= len;
        frame.lastMoveY /= len;
    }

    private static boolean isOverHotbar(GameContext ctx) {
        return ctx.hotbar.hitTest(com.badlogic.gdx.Gdx.input.getX(), com.badlogic.gdx.Gdx.input.getY()) != null;
    }

    private static boolean isInteracting(GameContext ctx, boolean overHotbar) {
        return !overHotbar
                && (ctx.mining.isActive()
                        || ctx.eat.isInteracting()
                        || ctx.placement.isInteracting()
                        || (ctx.input.placeHeld()
                                && ctx.interactionPresentation.hasValidPlacementPreview()));
    }

    private static boolean isMoving(float moveX, float moveY) {
        return moveX != 0f || moveY != 0f;
    }

    private static PlayerAnimContext buildAnimContext(FrameState frame, Entity player) {
        return new PlayerAnimContext(
                frame.moving,
                frame.interacting,
                player.getX(),
                player.getY(),
                frame.lastMoveX,
                frame.lastMoveY,
                frame.target);
    }
}
