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

/** Always-visible HUD row for every inventory slot (flat index 0..{@link InventoryConstants#SIZE}-1). */
public class Hotbar implements Disposable {
    private static final GameSettings.UiSize HOTBAR_UI_SIZE = GameSettings.UiSize.SMALL;
    public static final int SLOT_COUNT = InventoryConstants.SIZE;
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
        return GameSettings.slotMultiplier(HOTBAR_UI_SIZE);
    }

    private float slotPx() {
        return BASE_SLOT_PX * slotMultiplier();
    }

    private float gapPx() {
        return BASE_GAP_PX * slotMultiplier();
    }

    public void update(boolean lockSelection) {
        if (lockSelection) {
            return;
        }
        for (int i = 0; i < HOTKEYS.length; i++) {
            if (i < SLOT_COUNT && Gdx.input.isKeyJustPressed(HOTKEYS[i])) {
                inventory.setSelectedIndex(i);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
            inventory.cycleSelectedIndex(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
            inventory.cycleSelectedIndex(1);
        }
    }

    /** Called from scroll wheel events only (see InputController.scrolled). */
    public void applyScroll(float amountY, boolean lockSelection) {
        if (lockSelection) {
            return;
        }
        if (amountY > 0f) {
            inventory.cycleSelectedIndex(-1);
        } else if (amountY < 0f) {
            inventory.cycleSelectedIndex(1);
        }
    }

    public boolean handleClick(float screenX, float screenY) {
        Integer slotIndex = hitTest(screenX, screenY);
        if (slotIndex != null) {
            inventory.setSelectedIndex(slotIndex);
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
        int index = (int) (relX / step);
        if (index < 0 || index >= SLOT_COUNT) {
            return null;
        }
        float inSlot = relX - index * step;
        if (inSlot > slotPx()) {
            return null;
        }
        return index;
    }

    /** HUD bounds for slot {@code index} (0 = leftmost). Call after layout is current. */
    public void slotBounds(int index, Rectangle out) {
        layoutBounds();
        float slot = slotPx();
        float step = slot + gapPx();
        float x = bounds.x + index * step;
        out.set(x, bounds.y, slot, slot);
    }

    public Rectangle barBounds() {
        layoutBounds();
        return bounds;
    }

    public ItemStack getHeld() {
        return inventory.getHeld();
    }

    private void layoutBounds() {
        int mult = slotMultiplier();
        float slot = slotPx();
        float totalW = SLOT_COUNT * slot + (SLOT_COUNT - 1) * gapPx();
        float startX = (Constants.HUD_WIDTH_PX - totalW) / 2f;
        bounds.set(startX, BASE_BOTTOM_MARGIN * mult, totalW, slot);
    }

    public void render() {
        int selectedIndex = inventory.getSelectedIndex();
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
        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = startX + i * step;
            boolean selected = i == selectedIndex;
            TextureRegion bg = selected ? assets.uiCommon.slotSelected : assets.uiCommon.slot;
            hud.batch.draw(bg, x, y, slot, slot);

            ItemStack stack = inventory.getSlotAtIndex(i);
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

        BitmapFont font = hud.fonts.forUiSize(HOTBAR_UI_SIZE);
        float fontScale = DawnFonts.drawScaleForUiSize(HOTBAR_UI_SIZE);
        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = startX + i * step;
            ItemStack stack = inventory.getSlotAtIndex(i);
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
