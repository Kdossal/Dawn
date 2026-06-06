package com.dawn.ui.inventory;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

/** 16×16 inventory slot at 1× (parent scales). */
public final class ItemSlotWidget extends Table {
    private final Image icon;
    private final Label countLabel;
    private final Label hintLabel;
    private final InventorySlotRef slotRef;

    public ItemSlotWidget(
            DawnAssets assets,
            DawnFonts fonts,
            InventorySlotRef slotRef,
            String emptyHint,
            TextureRegion slotTexture) {
        this.slotRef = slotRef;
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);

        icon = new Image();
        icon.setScaling(Scaling.fit);
        icon.setAlign(Align.center);
        countLabel = InventoryUiStyle.label("", fonts, DawnTypography.SLOT_COUNT);
        countLabel.setAlignment(Align.bottom | Align.right);
        hintLabel = InventoryUiStyle.label(emptyHint, fonts, DawnTypography.SLOT_COUNT);
        hintLabel.setAlignment(Align.center);

        Image bg = new Image(InventoryUiStyle.fixedDrawable(slotTexture));
        float iconPx = InventoryDesign.slotIconPx();
        Container<Image> iconWrap = new Container<>(icon);
        iconWrap.size(iconPx, iconPx);
        iconWrap.align(Align.center);

        Table iconLayer = new Table();
        iconLayer.setFillParent(true);
        iconLayer.add(iconWrap).center().expand();
        Table hintLayer = new Table();
        hintLayer.setFillParent(true);
        hintLayer.add(hintLabel).center().expand();

        Stack overlay = new Stack();
        overlay.add(iconLayer);
        overlay.add(hintLayer);
        Table countWrap = new Table();
        countWrap.setFillParent(true);
        countWrap.add(countLabel).expand().bottom().right().pad(1f);
        overlay.add(countWrap);

        Stack stack = new Stack();
        stack.add(bg);
        stack.add(overlay);
        add(stack).size(InventoryDesign.SLOT_PX, InventoryDesign.SLOT_PX);
        setSize(InventoryDesign.SLOT_PX, InventoryDesign.SLOT_PX);
    }

    public InventorySlotRef slotRef() {
        return slotRef;
    }

    public void refresh(ItemStack itemStack, DawnAssets assets) {
        if (itemStack == null || itemStack.isEmpty()) {
            icon.setDrawable(null);
            icon.setVisible(false);
            hintLabel.setVisible(true);
            countLabel.setText("");
            return;
        }
        hintLabel.setVisible(false);
        icon.setVisible(true);
        ItemDef def = ItemRegistry.get(itemStack);
        TextureRegion region = def == null ? null : assets.item(def.iconId());
        if (region != null) {
            icon.setDrawable(new TextureRegionDrawable(region));
        } else {
            icon.setDrawable(null);
        }
        countLabel.setText(itemStack.count > 1 ? String.valueOf(itemStack.count) : "");
    }
}
