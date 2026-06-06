package com.dawn.ui.equipment;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.dawn.assets.DawnAssets;
import com.dawn.ui.DawnFonts;
import com.dawn.ui.DawnTypography.TextTier;
import com.dawn.ui.inventory.InventoryDesign;
import com.dawn.ui.inventory.InventoryUiStyle;

/** Wide stat row box (name + value on one centered row). */
public final class StatCellWidget extends Group {
    private static final float ROW_PAD_X = 3f;
    private static final float ROW_H = 12f;

    private final Label nameLabel;
    private final Label valueLabel;

    public StatCellWidget(DawnAssets assets, DawnFonts fonts) {
        setSize(InventoryDesign.STAT_BOX_W, InventoryDesign.STAT_BOX_H);

        Image bg = new Image(InventoryUiStyle.fixedDrawable(assets.uiEquipment.statCell));
        bg.setSize(InventoryDesign.STAT_BOX_W, InventoryDesign.STAT_BOX_H);
        addActor(bg);

        nameLabel = InventoryUiStyle.label("", fonts, TextTier.MD);
        nameLabel.setAlignment(Align.left);
        valueLabel = InventoryUiStyle.label("", fonts, TextTier.MD);
        valueLabel.setAlignment(Align.right);

        Table row = new Table();
        row.setSize(InventoryDesign.STAT_BOX_W - ROW_PAD_X * 2f, ROW_H);
        row.add(nameLabel).expandX().left();
        row.add(valueLabel).right();
        row.setPosition(ROW_PAD_X, (InventoryDesign.STAT_BOX_H - ROW_H) / 2f);
        addActor(row);
    }

    public void setStat(String name, int value) {
        nameLabel.setText(name);
        valueLabel.setText(String.valueOf(value));
    }
}
