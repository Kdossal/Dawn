package com.dawn.world.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dawn.assets.DawnAssets;

/** One Y-sorted world sprite (block, entity, or drop). */
public sealed interface WorldDrawable
        permits BlockWorldDrawable, EntityWorldDrawable, DropWorldDrawable {
    /** Bottom edge of sprite footprint in cells (higher = further north, drawn behind when descending). */
    float sortY();

    /** Right edge of sprite footprint in cells (lower = further west, drawn behind when ascending). */
    float sortX();

    /** Tie-break when sort cell matches; lower draws first (behind). */
    int depthBias();

    void draw(SpriteBatch batch, DawnAssets assets, DrawContext context);
}
