package com.dawn.world.render;

import com.dawn.config.Constants;
import com.dawn.entity.EntityBounds;
import com.dawn.render.SpriteAlphaMask;

/** Pixel-accurate behind-player test for occlusion fade. */
public final class OcclusionFade {
    private OcclusionFade() {}

    /**
     * True when the player sprite sits north of the block base and opaque pixels of both sprites overlap.
     *
     * @param playerOriginX rounded LibGDX draw origin for the player sprite
     * @param blockRectPx block draw rect {@code [px, py, w, h]} from {@link com.dawn.world.block.visual.BlockVisualLayout}
     */
    public static boolean playerBehindOccluder(
            EntityBounds player,
            int playerOriginX,
            int playerOriginY,
            int playerSpriteWidthPx,
            int playerSpriteHeightPx,
            SpriteAlphaMask playerMask,
            SpriteAlphaMask blockMask,
            float[] blockRectPx) {
        float blockBottomCell = blockRectPx[1] / Constants.CELL_SIZE_PX;
        if (player.spriteBottom <= blockBottomCell) {
            return false;
        }

        int playerRight = playerOriginX + playerSpriteWidthPx;
        int playerTop = playerOriginY + playerSpriteHeightPx;
        int blockLeft = Math.round(blockRectPx[0]);
        int blockBottom = Math.round(blockRectPx[1]);
        int blockRight = blockLeft + Math.round(blockRectPx[2]);
        int blockTop = blockBottom + Math.round(blockRectPx[3]);
        if (playerRight <= blockLeft
                || playerOriginX >= blockRight
                || playerTop <= blockBottom
                || playerOriginY >= blockTop) {
            return false;
        }

        return SpriteAlphaMask.opaqueOverlap(
                playerMask, playerOriginX, playerOriginY, blockMask, blockRectPx[0], blockRectPx[1]);
    }

    /** Coarse fallback when alpha masks are unavailable (e.g. headless tests). */
    public static boolean movementBoxBehindSprite(
            EntityBounds player,
            com.dawn.world.block.visual.BlockVisualDef visual,
            int cellX,
            int cellY) {
        float[] footprint =
                com.dawn.world.block.visual.BlockVisualLayout.spriteFootprintCell(visual, cellX, cellY);
        return movementBoxBehindFootprint(
                player, footprint[0], footprint[1], footprint[2], footprint[3]);
    }

    public static boolean movementBoxBehindFootprint(
            EntityBounds player, float spriteLeft, float spriteBottom, float spriteRight, float spriteTop) {
        boolean overlapX = player.moveLeft < spriteRight && player.moveRight > spriteLeft;
        if (!overlapX) {
            return false;
        }
        boolean overlapY = player.moveBottom < spriteTop && player.moveTop > spriteBottom;
        if (!overlapY) {
            return false;
        }
        return player.moveBottom > spriteBottom;
    }
}
