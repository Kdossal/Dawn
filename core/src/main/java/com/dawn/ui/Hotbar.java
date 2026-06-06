package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.render.RenderColors;

public class Hotbar implements Disposable {
    public static final float SLOT_W = 48f;
    public static final float SLOT_H = 48f;
    public static final float GAP = 6f;
    public static final float BAR_PAD = 10f;

    private static final int[] HOTKEYS = {
        Input.Keys.NUM_1,
        Input.Keys.NUM_2,
        Input.Keys.NUM_3,
        Input.Keys.NUM_4,
        Input.Keys.NUM_5,
        Input.Keys.NUM_6,
        Input.Keys.NUM_7,
        Input.Keys.NUM_8,
        Input.Keys.NUM_9,
        Input.Keys.NUM_0
    };

    private final HudAssets hud;
    private final DawnAssets assets;
    private final PlayerInventory inventory;
    private final Rectangle bounds = new Rectangle();
    private final Rectangle barBounds = new Rectangle();

    public Hotbar(HudAssets hud, DawnAssets assets, PlayerInventory inventory) {
        this.hud = hud;
        this.assets = assets;
        this.inventory = inventory;
    }

    public void update() {
        for (int i = 0; i < HOTKEYS.length; i++) {
            if (Gdx.input.isKeyJustPressed(HOTKEYS[i])) {
                inventory.setSelectedCol(i);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
            inventory.cycleRow(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
            inventory.cycleRow(1);
        }
    }

    /** Called from scroll wheel events only (see InputController.scrolled). */
    public void applyScroll(float amountY) {
        if (amountY > 0f) {
            cycleCol(-1);
        } else if (amountY < 0f) {
            cycleCol(1);
        }
    }

    public boolean handleClick(float screenX, float screenY) {
        Integer col = hitTest(screenX, screenY);
        if (col != null) {
            inventory.setSelectedCol(col);
            return true;
        }
        return false;
    }

    public Integer hitTest(float screenX, float screenY) {
        layoutBounds();
        if (!bounds.contains(screenX, screenY)) {
            return null;
        }
        float relX = screenX - bounds.x;
        float step = SLOT_W + GAP;
        int col = (int) (relX / step);
        if (col < 0 || col >= InventoryConstants.COLS) {
            return null;
        }
        float inSlot = relX - col * step;
        if (inSlot > SLOT_W) {
            return null;
        }
        return col;
    }

    public ItemStack getHeld() {
        return inventory.getHeld();
    }

    private void cycleCol(int delta) {
        int col = inventory.getSelectedCol() + delta;
        if (col < 0) {
            col = InventoryConstants.COLS - 1;
        } else if (col >= InventoryConstants.COLS) {
            col = 0;
        }
        inventory.setSelectedCol(col);
    }

    private void layoutBounds() {
        float totalW = InventoryConstants.COLS * SLOT_W + (InventoryConstants.COLS - 1) * GAP;
        float startX = (Constants.HUD_WIDTH_PX - totalW) / 2f;
        float y = 8f;
        bounds.set(startX, y, totalW, SLOT_H);
        barBounds.set(
                startX - BAR_PAD,
                y - BAR_PAD,
                totalW + BAR_PAD * 2f,
                SLOT_H + BAR_PAD * 2f);
    }

    public void render() {
        ItemStack[] row = inventory.getActiveRowSlots();
        int selectedCol = inventory.getSelectedCol();

        float totalW = InventoryConstants.COLS * SLOT_W + (InventoryConstants.COLS - 1) * GAP;
        float startX = (Constants.HUD_WIDTH_PX - totalW) / 2f;
        float y = 8f;
        layoutBounds();

        hud.shapes.begin(ShapeRenderer.ShapeType.Filled);
        hud.shapes.setColor(RenderColors.HOTBAR_BAR_BG);
        hud.shapes.rect(barBounds.x, barBounds.y, barBounds.width, barBounds.height);
        hud.shapes.end();

        hud.batch.begin();
        hud.batch.setColor(Color.WHITE);
        for (int i = 0; i < InventoryConstants.COLS; i++) {
            float x = startX + i * (SLOT_W + GAP);
            boolean selected = i == selectedCol;
            TextureRegion bg = selected ? assets.uiCommon.slotSelected : assets.uiCommon.slot;
            hud.batch.draw(bg, x, y, SLOT_W, SLOT_H);

            ItemStack stack = row[i];
            if (!stack.isEmpty()) {
                ItemDef def = ItemRegistry.get(stack);
                if (def != null) {
                    TextureRegion icon = assets.item(def.iconId());
                    if (icon != null) {
                        float iconSize = SlotUi.iconPxForSlot(SLOT_W);
                        float ix = x + (SLOT_W - iconSize) / 2f;
                        float iy = y + (SLOT_H - iconSize) / 2f;
                        hud.batch.draw(icon, ix, iy, iconSize, iconSize);
                    }
                }
            }
        }
        hud.batch.end();

        hud.batch.begin();
        for (int i = 0; i < InventoryConstants.COLS; i++) {
            float x = startX + i * (SLOT_W + GAP);
            String keyLabel = i == 9 ? "0" : String.valueOf(i + 1);
            DawnTypography.draw(
                    hud.batch,
                    hud.font,
                    hud.layout,
                    keyLabel,
                    DawnTypography.TextTier.XS,
                    DawnTypography.TextContext.HUD,
                    x + 4f,
                    y + SLOT_H - 6f,
                    RenderColors.HOTBAR_KEY_LABEL);

            ItemStack stack = row[i];
            if (!stack.isEmpty() && stack.count > 1) {
                String countLabel = String.valueOf(stack.count);
                DawnTypography.layout(
                        hud.layout,
                        hud.font,
                        countLabel,
                        DawnTypography.TextTier.XS,
                        DawnTypography.TextContext.HUD);
                DawnTypography.draw(
                        hud.font,
                        hud.batch,
                        hud.layout,
                        x + SLOT_W - hud.layout.width - 4f,
                        y + 10f,
                        DawnTypography.TextTier.XS,
                        DawnTypography.TextContext.HUD,
                        Color.WHITE);
            }
        }
        hud.batch.end();
    }

    @Override
    public void dispose() {}
}
