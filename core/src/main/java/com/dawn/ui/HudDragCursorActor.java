package com.dawn.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.dawn.assets.DawnAssets;
import com.dawn.item.ItemStack;
import com.dawn.ui.inventory.InventoryOverlayDesign;

/** Floating held-item icon for HUD drag sessions. */
public final class HudDragCursorActor extends Group {
    private static final Vector2 TMP = new Vector2();

    private final DawnFonts fonts;
    private final HudItemSlot slot;
    private float sizePx;

    public HudDragCursorActor(DawnAssets assets, DawnFonts fonts) {
        this.fonts = fonts;
        setVisible(false);
        setTouchable(Touchable.disabled);
        slot = new HudItemSlot(assets, fonts, HudSlotChrome.FLOATING);
        addActor(slot);
        useHudLayout();
    }

    public void useHudLayout() {
        setLayoutSize(HudSlotDesign.slotPx(), HudSlotDesign.iconPx());
        slot.setCountStyle(fonts, DawnTypography.SLOT_COUNT, HudSlotDesign.countPadPx());
    }

    public void useInventoryLayout() {
        setLayoutSize(InventoryOverlayDesign.slotPx(), InventoryOverlayDesign.iconPx());
        slot.setCountStyle(
                fonts, DawnTypography.INVENTORY_SLOT_COUNT, InventoryOverlayDesign.countPadPx());
    }

    public void setLayoutSize(float slotPx, float iconPx) {
        sizePx = slotPx;
        slot.setLayoutSize(slotPx, iconPx);
        setSize(slotPx, slotPx);
    }

    public void refresh(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            setVisible(false);
            slot.refresh(ItemStack.empty());
            return;
        }
        setVisible(true);
        slot.refresh(stack);
    }

    public void followMouse(Stage stage) {
        if (!isVisible()) {
            return;
        }
        TMP.set(com.badlogic.gdx.Gdx.input.getX(), com.badlogic.gdx.Gdx.input.getY());
        stage.screenToStageCoordinates(TMP);
        setPosition(TMP.x - sizePx * 0.5f, TMP.y - sizePx * 0.5f);
    }
}
