package com.dawn.entity.sprite;

import com.dawn.gameplay.TargetResolver.TargetCell;

/** Selects clip id and facing from movement / interaction context. */
public final class EntityAnimResolver {
    public record Selection(String clipId, Facing4 facing) {}

    private EntityAnimResolver() {}

    public static Selection selectClip(PlayerAnimContext ctx, Facing4 currentFacing) {
        Facing4 facing = resolveFacing(ctx, currentFacing);
        if (ctx.interacting()) {
            return new Selection(facing.interactClipId(), facing);
        }
        if (ctx.moving()) {
            return new Selection(facing.walkClipId(), facing);
        }
        return new Selection(facing.idleClipId(), facing);
    }

    public static int frameIndex(EntityAnimClip clip, float stateTime) {
        int index = (int) Math.floor(stateTime * clip.fps());
        return Math.floorMod(index, clip.frameCount());
    }

    private static Facing4 resolveFacing(PlayerAnimContext ctx, Facing4 currentFacing) {
        if (ctx.interacting() && ctx.target() != null) {
            return facingToward(ctx.feetX(), ctx.feetY(), ctx.target());
        }
        if (ctx.moving()) {
            return facingFromVector(ctx.moveX(), ctx.moveY());
        }
        return currentFacing == null ? Facing4.DOWN : currentFacing;
    }

    static Facing4 facingFromVector(float dx, float dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx >= 0f ? Facing4.RIGHT : Facing4.LEFT;
        }
        return dy >= 0f ? Facing4.UP : Facing4.DOWN;
    }

    static Facing4 facingToward(float playerX, float playerY, TargetCell target) {
        return facingFromVector(target.x() + 0.5f - playerX, target.y() + 0.5f - playerY);
    }
}
