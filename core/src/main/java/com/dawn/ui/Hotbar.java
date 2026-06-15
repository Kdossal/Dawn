package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.render.GameSettings;

public class Hotbar implements Disposable {
    public static final int COLS = InventoryConstants.COLS;
    /** Art-base (1×) sizes matching PNG dimensions; multiplied by {@link GameSettings#slotMultiplier}. */
    public static final float BASE_SLOT_PX = 20f;
    public static final float BASE_GAP_PX = 2f;
    public static final float BASE_ICON_PX = 16f;
    public static final float BASE_EDGE_PAD = 2.5f;
    public static final float BASE_BOTTOM_MARGIN = 4f;

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
    private final GameSettings settings;
    private final Rectangle bounds = new Rectangle();

    public Hotbar(HudAssets hud, DawnAssets assets, PlayerInventory inventory, GameSettings settings) {
        this.hud = hud;
        this.assets = assets;
        this.inventory = inventory;
        this.settings = settings;
    }

    private int slotMultiplier() {
        return GameSettings.slotMultiplier(settings.uiSize);
    }

    private float slotPx() {
        return BASE_SLOT_PX * slotMultiplier();
    }

    private float gapPx() {
        return BASE_GAP_PX * slotMultiplier();
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
        float step = slotPx() + gapPx();
        int col = (int) (relX / step);
        if (col < 0 || col >= COLS) {
            return null;
        }
        float inSlot = relX - col * step;
        if (inSlot > slotPx()) {
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
            col = COLS - 1;
        } else if (col >= COLS) {
            col = 0;
        }
        inventory.setSelectedCol(col);
    }

    private void layoutBounds() {
        int mult = slotMultiplier();
        float slot = slotPx();
        float totalW = COLS * slot + (COLS - 1) * gapPx();
        float startX = (Constants.HUD_WIDTH_PX - totalW) / 2f;
        bounds.set(startX, BASE_BOTTOM_MARGIN * mult, totalW, slot);
    }

    public void render() {
        ItemStack[] row = inventory.getActiveRowSlots();
        int selectedCol = inventory.getSelectedCol();
        layoutBounds();
        int mult = slotMultiplier();
        float slot = slotPx();
        float step = slot + gapPx();
        float icon = BASE_ICON_PX * mult;
        float pad = BASE_EDGE_PAD * mult;
        float startX = bounds.x;
        float y = bounds.y;

        hud.batch.begin();
        hud.batch.setColor(Color.WHITE);
        for (int i = 0; i < COLS; i++) {
            float x = startX + i * step;
            boolean selected = i == selectedCol;
            TextureRegion bg = selected ? assets.uiCommon.slotSelected : assets.uiCommon.slot;
            hud.batch.draw(bg, x, y, slot, slot);

            ItemStack stack = row[i];
            if (!stack.isEmpty()) {
                ItemDef def = ItemRegistry.get(stack);
                if (def != null) {
                    TextureRegion iconRegion = assets.item(def.iconId());
                    if (iconRegion != null) {
                        float ix = x + (slot - icon) / 2f;
                        float iy = y + (slot - icon) / 2f;
                        hud.batch.draw(iconRegion, ix, iy, icon, icon);
                    }
                }
            }
        }

        BitmapFont font = hud.fonts.forUiSize(settings.uiSize);
        float fontScale = DawnFonts.drawScaleForUiSize(settings.uiSize);
        for (int i = 0; i < COLS; i++) {
            float x = startX + i * step;
            String keyLabel = i == 9 ? "0" : String.valueOf(i + 1);
            drawLabel(font, fontScale, keyLabel, x + pad, y + slot - pad, Color.GRAY);

            ItemStack stack = row[i];
            if (!stack.isEmpty() && stack.count > 1) {
                String countLabel = String.valueOf(stack.count);
                font.getData().setScale(fontScale);
                font.setColor(Color.WHITE);
                hud.layout.setText(font, countLabel);
                float cx = x + slot - pad - hud.layout.width;
                float cy = y + pad + hud.layout.height;
                drawLabel(font, fontScale, countLabel, cx, cy, Color.WHITE);
            }
        }
        font.getData().setScale(1f);
        hud.batch.end();
    }

    private void drawLabel(BitmapFont font, float fontScale, String text, float x, float y, Color color) {
        font.getData().setScale(fontScale);
        font.setColor(color);
        hud.layout.setText(font, text);
        font.draw(hud.batch, hud.layout, x, y);
    }

    @Override
    public void dispose() {}
}
