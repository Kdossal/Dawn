package com.dawn.world.render;

import com.dawn.assets.DawnAssets;
import com.dawn.entity.sprite.EntitySpriteFrame;
import com.dawn.entity.EntityBounds;
import com.dawn.world.World;
import java.util.List;

/** Per-frame draw state for Y-sorted world sprites. */
public final class DrawContext {
    private final World world;
    private final OcclusionFadePlan fadePlan;
    private final float pixelAlignOffsetX;
    private final float pixelAlignOffsetY;

    private DrawContext(
            World world, OcclusionFadePlan fadePlan, float pixelAlignOffsetX, float pixelAlignOffsetY) {
        this.world = world;
        this.fadePlan = fadePlan;
        this.pixelAlignOffsetX = pixelAlignOffsetX;
        this.pixelAlignOffsetY = pixelAlignOffsetY;
    }

    public static DrawContext create(
            World world,
            List<WorldDrawable> drawables,
            EntityBounds playerBounds,
            float playerFeetX,
            float playerFeetY,
            EntitySpriteFrame playerSprite,
            DawnAssets assets,
            boolean occlusionFadeEnabled,
            float pixelAlignOffsetX,
            float pixelAlignOffsetY) {
        OcclusionFadePlan fadePlan =
                occlusionFadeEnabled
                        ? OcclusionFadePlan.build(
                                drawables,
                                playerBounds,
                                playerFeetX,
                                playerFeetY,
                                playerSprite,
                                assets)
                        : OcclusionFadePlan.disabled();
        return new DrawContext(world, fadePlan, pixelAlignOffsetX, pixelAlignOffsetY);
    }

    public World world() {
        return world;
    }

    public OcclusionFadePlan fadePlan() {
        return fadePlan;
    }

    public float pixelAlignOffsetX() {
        return pixelAlignOffsetX;
    }

    public float pixelAlignOffsetY() {
        return pixelAlignOffsetY;
    }
}
