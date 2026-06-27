package com.dawn.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.dawn.assets.DawnAssets;
import com.dawn.item.ItemStack;

/** Floating held-item icon for HUD drag sessions. */
public final class HudDragCursorActor extends Group {
    private static final Vector2 TMP = new Vector2();

    private final HudItemSlot slot;
    private float sizePx;

    public HudDragCursorActor(DawnAssets assets, DawnFonts fonts) {
        setVisible(false);
        setTouchable(Touchable.disabled);
        slot = new HudItemSlot(assets, fonts, HudSlotChrome.FLOATING);
        addActor(slot);
        layout();
    }

    public void layout() {
        sizePx = HudSlotDesign.slotPx();
        float iconPx = HudSlotDesign.iconPx();
        slot.setLayoutSize(sizePx, iconPx);
        setSize(sizePx, sizePx);
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
