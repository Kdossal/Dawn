package com.dawn.render;

import com.dawn.render.TileLighting.TileLight;

/**
 * Four corner RGB tints for one sprite quad (LibGDX y-up: bottomLeft = min x/y pixel corner).
 * Corner mapping matches LightMap vertex coordinates: BL=(cellX,cellY), BR=(cellX+1,cellY),
 * TL=(cellX,cellY+1), TR=(cellX+1,cellY+1).
 */
public record TileLightCorners(
        TileLight bottomLeft, TileLight bottomRight, TileLight topLeft, TileLight topRight) {

    public static TileLightCorners uniform(TileLight center) {
        return new TileLightCorners(center, center, center, center);
    }

    /** Legacy helper used by fallback paths. */
    public static TileLightCorners fromCenterSample(TileLight center) {
        return uniform(center);
    }

    /** Fixed left-bright / right-dark gradient for pipeline verification. */
    public static TileLightCorners debugGradient() {
        TileLight left = new TileLight(1f, 0.85f, 0.7f);
        TileLight right = new TileLight(0.15f, 0.15f, 0.18f);
        return new TileLightCorners(left, right, left, right);
    }
}
