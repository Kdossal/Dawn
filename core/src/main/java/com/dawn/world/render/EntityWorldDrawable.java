package com.dawn.world.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.entity.sprite.EntitySpriteFrame;
import com.dawn.render.SpriteAnchor;
import com.dawn.render.TileLighting;

public final class EntityWorldDrawable implements WorldDrawable {
    private final float feetX;
    private final float feetY;
    private final EntitySpriteFrame sprite;
    private final float sortY;
    private final float sortX;

    public EntityWorldDrawable(float feetX, float feetY, EntitySpriteFrame sprite) {
        this.feetX = feetX;
        this.feetY = feetY;
        this.sprite = sprite;
        int widthPx = sprite == null ? Constants.PLAYER_SPRITE_WIDTH_PX : sprite.widthPx();
        float[] key = WorldSortKeys.entityBottomRight(feetX, feetY, widthPx);
        this.sortY = key[0];
        this.sortX = key[1];
    }

    /** Sort-only stub (tests); {@link #draw} is a no-op when sprite is null. */
    EntityWorldDrawable(float feetX, float feetY, int spriteWidthPx) {
        this.feetX = feetX;
        this.feetY = feetY;
        this.sprite = null;
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
        if (sprite == null || sprite.region() == null) {
            return;
        }
        TextureRegion region = sprite.region();
        float w = sprite.widthPx();
        float h = sprite.heightPx();
        if (w <= 0 || h <= 0) {
            return;
        }
        float[] origin = SpriteAnchor.feetBottomCenter(feetX, feetY, w, h);
        int cellX = (int) Math.floor(feetX);
        int cellY = (int) Math.floor(feetY);
        TileLighting.TileLight light = context.tileLight(cellX, cellY);
        batch.setColor(light.r(), light.g(), light.b(), 1f);
        if (sprite.flipX()) {
            float originX = w / 2f;
            batch.draw(region, origin[0], origin[1], originX, 0f, w, h, -1f, 1f, 0f);
        } else {
            batch.draw(region, origin[0], origin[1], w, h);
        }
        batch.setColor(Color.WHITE);
    }
}
