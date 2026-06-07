package com.dawn.ui;

import com.badlogic.gdx.Input;
import com.dawn.config.Constants;
import com.dawn.input.InputController;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.entity.Entity;
import com.dawn.gameplay.BreakTarget;
import com.dawn.world.World;
import com.dawn.world.block.Layer;

public class DebugOverlay implements Disposable {
    public static final int TOGGLE_KEY = Input.Keys.F3;

    private final HudAssets hud;
    private boolean visible = true;

    public DebugOverlay(HudAssets hud) {
        this.hud = hud;
    }

    public void toggle() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void render(
            InputController input,
            Entity entity,
            PlayerInventory inventory,
            float moveX,
            float moveY,
            float delta,
            String interactionMessage,
            long simTick,
            int activeSimRegions,
            int pendingGrassEvents,
            int pendingBushEvents,
            Boolean hoverSimActive,
            int screenX,
            int screenY,
            World world,
            BreakTarget hoverBreak) {
        if (!visible) {
            return;
        }

        drawKeyIndicators(input, 12f, Constants.HUD_HEIGHT_PX - 28f);

        hud.batch.begin();
        float y = Constants.HUD_HEIGHT_PX - 52f;
        float line = 24f;

        y = line("F3: debug | hold LMB mine / RMB place | 1-0 hotbar", 8f, y, line);
        y = line(
                "Sim tick: "
                        + simTick
                        + "  regions: "
                        + activeSimRegions
                        + "  catch-up: "
                        + pendingGrassEvents
                        + "g/"
                        + pendingBushEvents
                        + "b  "
                        + (interactionMessage == null ? "" : interactionMessage),
                8f,
                y,
                line);
        y = line(
                "Move: "
                        + fmt(moveX)
                        + ", "
                        + fmt(moveY)
                        + "  spd: "
                        + fmt(entity.getMoveSpeedCellsPerSec(input.isRunning()))
                        + (input.isRunning() ? " RUN" : "")
                        + "  delta: "
                        + fmt(delta),
                8f,
                y,
                line);
        com.dawn.entity.EntityDef def = entity.def();
        int moveWpx = Math.round(def.moveWidthCells() * Constants.CELL_SIZE_PX);
        int moveHpx = Math.round(def.moveHeightCells() * Constants.CELL_SIZE_PX);
        y = line("Entity: " + fmt(entity.getX()) + ", " + fmt(entity.getY())
                + "  moved: " + entity.wasLastMoveApplied()
                + "  hp: " + fmt(entity.getCurrentHp()) + "/" + fmt(entity.getMaxHp()), 8f, y, line);
        y = line("Move box: " + moveWpx + "x" + moveHpx + " px  (F3: green=move, red=blocked cell)", 8f, y, line);
        y = line("Mouse: " + screenX + ", " + screenY, 8f, y, line);
        y = line(formatHoverBlock(world, hoverBreak, hoverSimActive), 8f, y, line);
        if (inventory != null) {
            y = line(
                    "Inv row: "
                            + (inventory.getActiveRow() + 1)
                            + "/"
                            + InventoryConstants.ROWS
                            + "  slot: "
                            + (inventory.getSelectedCol() + 1),
                    8f,
                    y,
                    line);
        }
        hud.batch.end();
    }

    private void drawKeyIndicators(InputController input, float x, float y) {
        float size = 22f;
        float gap = 6f;
        drawKeyBox(x + size + gap, y + size + gap, size, input, Input.Keys.W);
        drawKeyBox(x, y, size, input, Input.Keys.A);
        drawKeyBox(x + size + gap, y, size, input, Input.Keys.S);
        drawKeyBox(x + (size + gap) * 2, y, size, input, Input.Keys.D);
    }

    private void drawKeyBox(float x, float y, float size, InputController input, int key) {
        boolean on = input.isHeld(key);
        hud.shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        hud.shapes.setColor(on ? 0.2f : 0.15f, on ? 0.85f : 0.2f, on ? 0.3f : 0.2f, 0.9f);
        hud.shapes.rect(x, y, size, size);
        hud.shapes.end();
    }

    private float line(String text, float x, float y, float lineHeight) {
        DawnTypography.draw(
                hud.batch,
                hud.font,
                hud.layout,
                text,
                DawnTypography.TextTier.S,
                DawnTypography.TextContext.HUD,
                x,
                y,
                com.badlogic.gdx.graphics.Color.WHITE);
        return y - lineHeight;
    }

    private static String fmt(float v) {
        return String.format("%.3f", v);
    }

    private static String formatHoverBlock(World world, BreakTarget hoverBreak, Boolean hoverSimActive) {
        if (hoverBreak == null) {
            return "Hover block: —";
        }
        float max = hoverBreak.health();
        float remaining = world.getBlockDamage().getRemaining(hoverBreak.layer(), hoverBreak.x(), hoverBreak.y(), max);
        String layer =
                switch (hoverBreak.layer()) {
                    case GROUND -> "ground";
                    case FLOOR -> "floor";
                    case OBJECT -> "object";
                };
        String sim = hoverSimActive == null ? "" : (hoverSimActive ? "  sim:active" : "  sim:OFF");
        return "Hover block: "
                + hoverBreak.blockId().name().toLowerCase()
                + " ("
                + layer
                + " "
                + hoverBreak.x()
                + ","
                + hoverBreak.y()
                + ")  hp "
                + fmt(remaining)
                + "/"
                + fmt(max)
                + sim;
    }

    @Override
    public void dispose() {}
}
