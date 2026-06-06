package com.dawn.ui.inventory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.dawn.assets.DawnAssets;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.ui.DawnFonts;
import com.dawn.ui.DawnTypography;

/** Floating icon + stack count that follows the mouse while the inventory cursor holds items. */
public final class InventoryCursorActor extends Group {
    private static final Vector2 TMP = new Vector2();

    private final Image icon;
    private final Label countLabel;
    private final float sizePx;

    public InventoryCursorActor(DawnAssets assets, DawnFonts fonts) {
        sizePx = InventoryDesign.dragIconPx();
        setVisible(false);
        setTouchable(Touchable.disabled);

        icon = new Image();
        icon.setScaling(Scaling.fit);
        icon.setAlign(Align.center);

        countLabel = InventoryUiStyle.label("", fonts, DawnTypography.SLOT_COUNT);
        countLabel.setAlignment(Align.bottom | Align.right);
        // Slot labels live under an inventory-root 5x scale; cursor actor is HUD-space, so compensate.
        countLabel.setFontScale(countLabel.getFontScaleX() * InventoryDesign.UI_SCALE);

        float iconPx = InventoryDesign.slotIconPx() * InventoryDesign.UI_SCALE;
        Container<Image> iconWrap = new Container<>(icon);
        iconWrap.size(iconPx, iconPx);
        iconWrap.align(Align.center);

        Table iconLayer = new Table();
        iconLayer.setFillParent(true);
        iconLayer.add(iconWrap).center().expand();

        Table countWrap = new Table();
        countWrap.setFillParent(true);
        countWrap.add(countLabel).expand().bottom().right().pad(1f * InventoryDesign.UI_SCALE);

        Stack stack = new Stack();
        stack.add(iconLayer);
        stack.add(countWrap);
        stack.setSize(sizePx, sizePx);
        addActor(stack);
        setSize(sizePx, sizePx);
    }

    public void refresh(ItemStack stack, DawnAssets assets) {
        if (stack == null || stack.isEmpty()) {
            setVisible(false);
            icon.setDrawable(null);
            countLabel.setText("");
            return;
        }
        setVisible(true);
        ItemDef def = ItemRegistry.get(stack);
        if (def != null && assets.item(def.iconId()) != null) {
            icon.setDrawable(new TextureRegionDrawable(assets.item(def.iconId())));
        } else {
            icon.setDrawable(null);
        }
        countLabel.setText(stack.count > 1 ? String.valueOf(stack.count) : "");
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
