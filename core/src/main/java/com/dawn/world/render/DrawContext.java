package com.dawn.world.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.entity.EntityBounds;
import com.dawn.world.World;
import java.util.List;

/** Per-frame draw state for Y-sorted world sprites. */
public final class DrawContext {
    private final World world;
    private final OcclusionFadePlan fadePlan;

    private DrawContext(World world, OcclusionFadePlan fadePlan) {
        this.world = world;
        this.fadePlan = fadePlan;
    }

    public static DrawContext create(
            World world,
            List<WorldDrawable> drawables,
            EntityBounds playerBounds,
            float playerFeetX,
            float playerFeetY,
            TextureRegion playerSprite,
            DawnAssets assets,
            boolean occlusionFadeEnabled) {
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
        return new DrawContext(world, fadePlan);
    }

    public World world() {
        return world;
    }

    public OcclusionFadePlan fadePlan() {
        return fadePlan;
    }
}
