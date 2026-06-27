package com.dawn.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.config.Constants;

/** Art-base layout for in-world crafting slot chrome ({@code crafting_slot.png} quad, 80×20 at 1×). */
public final class CraftingSlotDesign {
    public static final int BASE_CELL_PX = 20;
    public static final int BASE_CHROME_W_PX = 80;
    public static final int BASE_CHROME_H_PX = 20;
    public static final float BASE_GAP_PX = 4f;

    public record ChromeRegions(
            TextureRegion slotBase,
            TextureRegion unavailable,
            TextureRegion selected,
            TextureRegion time) {}

    private CraftingSlotDesign() {}

    public static int artMult() {
        return Constants.HUD_ART_MULT;
    }

    public static float cellPx() {
        return BASE_CELL_PX * artMult();
    }

    public static float chromeWx() {
        return BASE_CHROME_W_PX * artMult();
    }

    public static float chromeHy() {
        return BASE_CHROME_H_PX * artMult();
    }

    public static float gapPx() {
        return BASE_GAP_PX * artMult();
    }

    /** Slices the full 80×20 crafting_slot sheet into base / unavailable / selected / time overlays. */
    public static ChromeRegions chromeRegions(TextureRegion full) {
        return new ChromeRegions(
                new TextureRegion(full, 0, 0, BASE_CELL_PX, BASE_CHROME_H_PX),
                new TextureRegion(full, BASE_CELL_PX, 0, BASE_CELL_PX, BASE_CHROME_H_PX),
                new TextureRegion(full, BASE_CELL_PX * 2, 0, BASE_CELL_PX, BASE_CHROME_H_PX),
                new TextureRegion(full, BASE_CELL_PX * 3, 0, BASE_CELL_PX, BASE_CHROME_H_PX));
    }
}
