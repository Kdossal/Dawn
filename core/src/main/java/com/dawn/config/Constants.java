package com.dawn.config;

/**
 * World and UI sizing.
 *
 * <p><b>Logical pixels</b> — gameplay/render space ({@link #VIEW_WIDTH_PX}×{@link #VIEW_HEIGHT_PX}).
 * One tile cell = {@link #CELL_SIZE_PX} logical px; source art is drawn at native texture size (typically
 * 16×16).
 *
 * <p><b>Screen pixels</b> — framebuffer/window ({@link #WINDOW_WIDTH}×{@link #WINDOW_HEIGHT}), logical
 * size × {@link #DISPLAY_SCALE} (integer upscale, nearest-filtered textures).
 *
 * <p><b>HUD pixels</b> — hotbar, F3 debug, inventory Scene2D ({@link #HUD_WIDTH_PX}×{@link #HUD_HEIGHT_PX}).
 * Matches window size; not scaled by {@link com.dawn.render.GameViewport} (world-only).
 */
public final class Constants {
    public static final int MAP_WIDTH = 100;
    public static final int MAP_HEIGHT = 100;

    public static final int VIEW_WIDTH_CELLS = 40;
    public static final int VIEW_HEIGHT_CELLS = 25;

    /** Logical pixels per world cell (1:1 with 16×16 source art). */
    public static final int CELL_SIZE_PX = 16;

    /** Integer scale from logical view to window (640×400 → 1280×800). */
    public static final int DISPLAY_SCALE = 2;

    public static final int VIEW_WIDTH_PX = VIEW_WIDTH_CELLS * CELL_SIZE_PX;
    public static final int VIEW_HEIGHT_PX = VIEW_HEIGHT_CELLS * CELL_SIZE_PX;

    public static final int WINDOW_WIDTH = VIEW_WIDTH_PX * DISPLAY_SCALE;
    public static final int WINDOW_HEIGHT = VIEW_HEIGHT_PX * DISPLAY_SCALE;

    /** HUD / inventory coordinate space (1:1 with window; pre-Part-A layout). */
    public static final int HUD_WIDTH_PX = WINDOW_WIDTH;
    public static final int HUD_HEIGHT_PX = WINDOW_HEIGHT;

    /** Default floor/base tile art size in pixels; blocks may use other sizes via {@link com.dawn.world.block.visual.BlockVisualDef}. */
    public static final int TILE_ART_PX = 16;

    /**
     * Default movement footprint width in cells (14 logical px). Slightly narrower than a full tile so
     * the body fits 1-cell-wide gaps when centered in a corridor cell.
     */
    public static final float DEFAULT_MOVE_WIDTH_CELLS = 14f / CELL_SIZE_PX;

    /**
     * Default movement footprint height in cells (14 logical px). Square 14×14 body for corridor fit.
     */
    public static final float DEFAULT_MOVE_HEIGHT_CELLS = 14f / CELL_SIZE_PX;

    public static final int MAP_WIDTH_PX = MAP_WIDTH * CELL_SIZE_PX;
    public static final int MAP_HEIGHT_PX = MAP_HEIGHT * CELL_SIZE_PX;

    private Constants() {}
}
