package com.dawn.world.render;

import com.dawn.assets.DawnAssets;
import com.dawn.entity.EntityBounds;
import com.dawn.render.RenderColors;
import com.dawn.render.SpriteAlphaMask;
import com.dawn.render.SpriteAnchor;
import com.dawn.world.CellPos;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.visual.BlockVisualDef;
import com.dawn.world.block.visual.BlockVisualLayout;
import com.dawn.world.block.visual.BlockVisualRegistry;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Per-frame fade state for blocks occluding the player.
 *
 * <p>Cost per frame: O(visible occluders) with a cheap behind/AABB reject, then pixel sampling only in
 * the sprite intersection (typically a few hundred pixels per tree/bush near the player).
 */
public final class OcclusionFadePlan {
    private static final OcclusionFadePlan DISABLED =
            new OcclusionFadePlan(Collections.emptySet(), false);

    private final Set<Long> fadedBlockCells;
    private final boolean active;

    private OcclusionFadePlan(Set<Long> fadedBlockCells, boolean active) {
        this.fadedBlockCells = fadedBlockCells;
        this.active = active;
    }

    public static OcclusionFadePlan disabled() {
        return DISABLED;
    }

    public static OcclusionFadePlan build(
            List<WorldDrawable> drawables,
            EntityBounds playerBounds,
            float playerFeetX,
            float playerFeetY,
            TextureRegion playerSprite,
            DawnAssets assets) {
        Set<Long> fadedCells = new HashSet<>();
        SpriteAlphaMask playerMask = assets.occlusionMasks.player();
        int playerW = playerSprite == null ? 0 : playerSprite.getRegionWidth();
        int playerH = playerSprite == null ? 0 : playerSprite.getRegionHeight();
        boolean pixelTest = playerMask != null && playerW > 0 && playerH > 0;

        int playerOriginX = 0;
        int playerOriginY = 0;
        if (pixelTest) {
            float[] playerOrigin =
                    SpriteAnchor.feetBottomCenter(playerFeetX, playerFeetY, playerW, playerH);
            playerOriginX = Math.round(playerOrigin[0]);
            playerOriginY = Math.round(playerOrigin[1]);
        }

        for (WorldDrawable drawable : drawables) {
            if (!(drawable instanceof BlockWorldDrawable block)) {
                continue;
            }
            if (!BlockDefinitions.triggersOcclusionFade(block.blockId())) {
                continue;
            }
            BlockVisualDef visual = BlockVisualRegistry.get(block.blockId());
            if (visual == null) {
                continue;
            }
            boolean behind;
            if (pixelTest) {
                SpriteAlphaMask blockMask = assets.occlusionMasks.block(visual.texture());
                if (blockMask == null) {
                    continue;
                }
                float[] blockRect = BlockVisualLayout.rectPx(visual, block.cellX(), block.cellY());
                behind =
                        OcclusionFade.playerBehindOccluder(
                                playerBounds,
                                playerOriginX,
                                playerOriginY,
                                playerW,
                                playerH,
                                playerMask,
                                blockMask,
                                blockRect);
            } else {
                behind =
                        OcclusionFade.movementBoxBehindSprite(
                                playerBounds, visual, block.cellX(), block.cellY());
            }
            if (behind) {
                fadedCells.add(CellPos.pack(block.cellX(), block.cellY()));
            }
        }
        return new OcclusionFadePlan(fadedCells, true);
    }

    public float blockDrawAlpha(BlockId blockId, int cellX, int cellY) {
        if (!active || !BlockDefinitions.fadeWhenPlayerBehind(blockId)) {
            return 1f;
        }
        if (fadedBlockCells.contains(CellPos.pack(cellX, cellY))) {
            return RenderColors.OCCLUSION_FADE_ALPHA;
        }
        return 1f;
    }
}
