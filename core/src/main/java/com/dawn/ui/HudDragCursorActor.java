package com.dawn.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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
import com.dawn.render.GameSettings;

/** Floating held-item icon for HUD drag sessions (always SMALL to match hotbar/sidebar). */
public final class HudDragCursorActor extends Group {
    private static final Vector2 TMP = new Vector2();
    private static final GameSettings.UiSize CURSOR_UI_SIZE = GameSettings.UiSize.SMALL;

    private final Image icon;
    private final Label countLabel;
    private final Stack stack;
    private final Container<Image> iconWrap;
    private float sizePx;

    public HudDragCursorActor(DawnAssets assets, DawnFonts fonts) {
        setVisible(false);
        setTouchable(Touchable.disabled);

        icon = new Image();
        icon.setScaling(Scaling.fit);
        icon.setAlign(Align.center);
        countLabel = new Label("", new Label.LabelStyle(fonts.forUiSize(CURSOR_UI_SIZE), Color.WHITE));
        countLabel.setAlignment(Align.bottom | Align.right);

        iconWrap = new Container<>(icon);
        iconWrap.align(Align.center);

        Table iconLayer = new Table();
        iconLayer.setFillParent(true);
        iconLayer.add(iconWrap).center().expand();

        Table countWrap = new Table();
        countWrap.setFillParent(true);
        countWrap.add(countLabel).expand().bottom().right().pad(2f);

        stack = new Stack();
        stack.add(iconLayer);
        stack.add(countWrap);
        addActor(stack);

        layoutForUiSize(CURSOR_UI_SIZE);
    }

    public void layoutForUiSize(GameSettings.UiSize uiSize) {
        int mult = GameSettings.slotMultiplier(CURSOR_UI_SIZE);
        sizePx = EquipmentSidebarDesign.BASE_SLOT_PX * mult;
        float iconPx = EquipmentSidebarDesign.BASE_ICON_PX * mult;
        iconWrap.size(iconPx, iconPx);
        stack.setSize(sizePx, sizePx);
        setSize(sizePx, sizePx);
        float fontScale = DawnFonts.drawScaleForUiSize(CURSOR_UI_SIZE);
        countLabel.setFontScale(fontScale);
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
