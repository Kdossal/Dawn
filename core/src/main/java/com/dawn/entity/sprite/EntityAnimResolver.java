package com.dawn.entity.sprite;

import com.dawn.gameplay.TargetResolver.TargetCell;

/** Selects clip id and facing from movement / interaction context. */
public final class EntityAnimResolver {
    public record Selection(String clipId, Facing2 facing) {}

    private EntityAnimResolver() {}

    public static Selection selectClip(PlayerAnimContext ctx, Facing2 currentFacing) {
        Facing2 facing = resolveFacing(ctx, currentFacing);
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

    private static Facing2 resolveFacing(PlayerAnimContext ctx, Facing2 currentFacing) {
        Facing2 fallback = currentFacing == null ? Facing2.LEFT : currentFacing;
        if (ctx.interacting() && ctx.target() != null) {
            return facingToward(ctx.feetX(), ctx.feetY(), ctx.target(), fallback);
        }
        if (ctx.interacting()) {
            return fallback;
        }
        if (ctx.moving()) {
            return facingFromVector(ctx.moveX(), fallback);
        }
        return fallback;
    }

    static Facing2 facingFromVector(float dx, Facing2 currentFacing) {
        if (dx > 0f) {
            return Facing2.RIGHT;
        }
        if (dx < 0f) {
            return Facing2.LEFT;
        }
        return currentFacing == null ? Facing2.LEFT : currentFacing;
    }

    static Facing2 facingToward(float playerX, float playerY, TargetCell target, Facing2 currentFacing) {
        float dx = target.x() + 0.5f - playerX;
        return facingFromVector(dx, currentFacing);
    }
}
