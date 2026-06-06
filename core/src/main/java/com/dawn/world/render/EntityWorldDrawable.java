package com.dawn.world.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.render.SpriteAnchor;

public final class EntityWorldDrawable implements WorldDrawable {
    private final float feetX;
    private final float feetY;
    private final TextureRegion texture;
    private final float sortY;
    private final float sortX;

    public EntityWorldDrawable(float feetX, float feetY, TextureRegion texture) {
        this(
                feetX,
                feetY,
                texture,
                texture == null ? 0 : texture.getRegionWidth());
    }

    /** Sort-only stub (tests); {@link #draw} is a no-op when texture is null. */
    EntityWorldDrawable(float feetX, float feetY, int spriteWidthPx) {
        this(feetX, feetY, null, spriteWidthPx);
    }

    private EntityWorldDrawable(float feetX, float feetY, TextureRegion texture, int spriteWidthPx) {
        this.feetX = feetX;
        this.feetY = feetY;
        this.texture = texture;
        float[] key = WorldSortKeys.entityBottomRight(feetX, feetY, spriteWidthPx);
        this.sortY = key[0];
        this.sortX = key[1];
    }

    @Override
    public float sortY() {
        return sortY;
    }

    @Override
    public float sortX() {
        return sortX;
    }

    @Override
    public int depthBias() {
        return WorldDepthBias.ENTITY;
    }

    @Override
    public void draw(SpriteBatch batch, DawnAssets assets, DrawContext context) {
        if (texture == null) {
            return;
        }
        float w = texture.getRegionWidth();
        float h = texture.getRegionHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        float[] origin = SpriteAnchor.feetBottomCenter(feetX, feetY, w, h);
        batch.setColor(Color.WHITE);
        batch.draw(texture, origin[0], origin[1], w, h);
    }
}
