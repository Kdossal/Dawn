package com.dawn.world.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dawn.assets.DawnAssets;
import com.dawn.gameplay.drops.DropRenderer;
import com.dawn.gameplay.drops.WorldDrop;
import com.dawn.render.TileLighting;

public final class DropWorldDrawable implements WorldDrawable {
    private final WorldDrop drop;
    private final float sortY;
    private final float sortX;

    public DropWorldDrawable(WorldDrop drop) {
        this.drop = drop;
        float[] key = WorldSortKeys.dropBottomRight(drop);
        this.sortY = key[0];
        this.sortX = key[1];
    }

    public WorldDrop drop() {
        return drop;
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
        return WorldDepthBias.DROP;
    }

    @Override
    public void draw(SpriteBatch batch, DawnAssets assets, DrawContext context) {
        int cellX = (int) Math.floor(drop.x);
        int cellY = (int) Math.floor(drop.y);
        TileLighting.TileLight light = context.tileLight(cellX, cellY);
        DropRenderer.drawOne(batch, assets, drop, light.r(), light.g(), light.b());
    }
}
