package com.dawn.entity.sprite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** One drawable entity animation frame. */
public record EntitySpriteFrame(TextureRegion region, boolean flipX, int widthPx, int heightPx) {}
