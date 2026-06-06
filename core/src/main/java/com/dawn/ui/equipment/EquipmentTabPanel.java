package com.dawn.ui.equipment;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.dawn.assets.DawnAssets;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.inventory.PlayerProfile;
import com.dawn.ui.DawnFonts;
import com.dawn.ui.DawnTypography.TextTier;
import com.dawn.ui.inventory.InventoryDesign;
import com.dawn.ui.inventory.InventoryCursorController;
import com.dawn.ui.inventory.InventorySlotRef;
import com.dawn.ui.inventory.InventoryUiStyle;
import com.dawn.ui.inventory.ItemSlotWidget;

/** Equipment tab: armor, character sheet, accessories, and stat grid. */
public final class EquipmentTabPanel extends Group {
    private final ItemSlotWidget[] wearSlots = new ItemSlotWidget[EquipmentSlot.wearOrder().length];
    private final ItemSlotWidget[] accessorySlots = new ItemSlotWidget[EquipmentSlot.accessoryOrder().length];
    private final StatCellWidget[] statCells = new StatCellWidget[PlayerProfile.STAT_NAMES.length];
    private final Label nameLabel;
    private final Image playerSprite;
    private final Label levelLabel;
    private final Image expBg;
    private final Image expFill;
    private final Label expLabel;

    public EquipmentTabPanel(DawnAssets assets, DawnFonts fonts) {
        setSize(InventoryDesign.TAB_PAGE_W, InventoryDesign.TAB_PAGE_H);

        EquipmentSlot[] wear = EquipmentSlot.wearOrder();
        for (int i = 0; i < wear.length; i++) {
            wearSlots[i] =
                    new ItemSlotWidget(
                            assets,
                            fonts,
                            InventorySlotRef.equipment(wear[i]),
                            "",
                            assets.uiCommon.slotEquip);
            wearSlots[i].setPosition(InventoryDesign.ARMOR_X, InventoryDesign.wearSlotY(i));
            addActor(wearSlots[i]);
        }

        float charX = InventoryDesign.CHAR_X;
        float nameY = InventoryDesign.PAGE_PAD + InventoryDesign.PAGE_INNER_H - 5f;
        nameLabel = InventoryUiStyle.label("", fonts, TextTier.MD);
        nameLabel.setAlignment(Align.center);
        nameLabel.setWidth(InventoryDesign.CHAR_W);
        nameLabel.setPosition(Math.round(charX), Math.round(nameY));
        addActor(nameLabel);

        playerSprite = new Image(new TextureRegionDrawable(assets.player));
        playerSprite.setScaling(Scaling.fit);
        float spriteY =
                nameY
                        - InventoryDesign.CHAR_NAME_TO_SPRITE_GAP
                        - InventoryDesign.CHAR_SPRITE_PX
                        - InventoryDesign.CHAR_SPRITE_DOWN_OFFSET;
        playerSprite.setSize(InventoryDesign.CHAR_SPRITE_PX, InventoryDesign.CHAR_SPRITE_PX);
        playerSprite.setPosition(
                Math.round(charX + (InventoryDesign.CHAR_W - InventoryDesign.CHAR_SPRITE_PX) / 2f),
                Math.round(spriteY));
        addActor(playerSprite);

        float levelY =
                spriteY - InventoryDesign.CHAR_SPRITE_TO_LEVEL_GAP - InventoryDesign.CHAR_LEVEL_H;
        levelLabel = InventoryUiStyle.label("", fonts, TextTier.MD);
        levelLabel.setAlignment(Align.center);
        levelLabel.setWidth(InventoryDesign.CHAR_W);
        levelLabel.setPosition(Math.round(charX), Math.round(levelY));
        addActor(levelLabel);

        float expX = charX + (InventoryDesign.CHAR_W - InventoryDesign.EXP_BAR_W) / 2f;
        float expBarY =
                levelY
                        - InventoryDesign.CHAR_LEVEL_TO_EXP_GAP
                        - InventoryDesign.EXP_BAR_H
                        - InventoryDesign.CHAR_EXP_BAR_DROP;
        expBg = new Image(InventoryUiStyle.fixedDrawable(assets.uiEquipment.expBarBg));
        expBg.setSize(InventoryDesign.EXP_BAR_W, InventoryDesign.EXP_BAR_H);
        expBg.setPosition(Math.round(expX), Math.round(expBarY));
        addActor(expBg);

        expFill = new Image(InventoryUiStyle.fixedDrawable(assets.uiEquipment.expBarFill));
        expFill.setPosition(Math.round(expX), Math.round(expBarY));
        addActor(expFill);

        expLabel = InventoryUiStyle.label("", fonts, TextTier.SM);
        expLabel.setAlignment(Align.center);
        expLabel.setWidth(InventoryDesign.CHAR_W);
        expLabel.setPosition(
                Math.round(charX),
                Math.round(
                        expBarY
                                - InventoryDesign.CHAR_EXP_TO_LABEL_GAP
                                - InventoryDesign.CHAR_EXP_LABEL_H
                                + InventoryDesign.CHAR_EXP_BAR_DROP));
        addActor(expLabel);

        EquipmentSlot[] accessories = EquipmentSlot.accessoryOrder();
        for (int i = 0; i < accessories.length; i++) {
            accessorySlots[i] =
                    new ItemSlotWidget(
                            assets,
                            fonts,
                            InventorySlotRef.equipment(accessories[i]),
                            "",
                            assets.uiCommon.slotEquip);
            accessorySlots[i].setPosition(
                    InventoryDesign.ACCESS_X, InventoryDesign.accessorySlotY(i));
            addActor(accessorySlots[i]);
        }

        for (int i = 0; i < PlayerProfile.STAT_NAMES.length; i++) {
            int row = i / InventoryDesign.STAT_COLS;
            int col = i % InventoryDesign.STAT_COLS;
            statCells[i] = new StatCellWidget(assets, fonts);
            statCells[i].setPosition(InventoryDesign.statBoxX(col), InventoryDesign.statBoxY(row));
            addActor(statCells[i]);
        }
    }

    public void registerCursorController(InventoryCursorController controller) {
        for (ItemSlotWidget slot : wearSlots) {
            controller.registerSlot(slot);
        }
        for (ItemSlotWidget slot : accessorySlots) {
            controller.registerSlot(slot);
        }
    }

    public ItemSlotWidget wearSlot(EquipmentSlot slot) {
        EquipmentSlot[] order = EquipmentSlot.wearOrder();
        for (int i = 0; i < order.length; i++) {
            if (order[i] == slot) {
                return wearSlots[i];
            }
        }
        return null;
    }

    public ItemSlotWidget accessorySlot(EquipmentSlot slot) {
        EquipmentSlot[] order = EquipmentSlot.accessoryOrder();
        for (int i = 0; i < order.length; i++) {
            if (order[i] == slot) {
                return accessorySlots[i];
            }
        }
        return null;
    }

    public ItemSlotWidget equipmentSlotWidget(EquipmentSlot slot) {
        if (slot.isAccessory()) {
            return accessorySlot(slot);
        }
        return wearSlot(slot);
    }

    public void refresh(PlayerProfile profile, EquipmentInventory equipment, DawnAssets assets) {
        nameLabel.setText(profile.name);
        levelLabel.setText("Lv " + profile.level);
        expLabel.setText("XP: " + profile.exp + " / " + profile.expToNext);
        float fillW = InventoryDesign.EXP_BAR_W * profile.expRatio();
        expFill.setSize(Math.max(0f, fillW), InventoryDesign.EXP_BAR_H);

        EquipmentSlot[] wear = EquipmentSlot.wearOrder();
        for (int i = 0; i < wear.length; i++) {
            wearSlots[i].refresh(equipment.get(wear[i]), assets);
        }
        EquipmentSlot[] accessories = EquipmentSlot.accessoryOrder();
        for (int i = 0; i < accessories.length; i++) {
            accessorySlots[i].refresh(equipment.get(accessories[i]), assets);
        }
        for (int i = 0; i < statCells.length; i++) {
            statCells[i].setStat(PlayerProfile.STAT_NAMES[i], profile.statValue(i));
        }
    }
}
