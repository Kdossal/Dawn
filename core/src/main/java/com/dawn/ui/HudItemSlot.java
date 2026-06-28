package com.dawn.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.dawn.assets.DawnAssets;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;

/** Canonical HUD item slot presentation: bg, stretched icon, stack count. */
public final class HudItemSlot extends Group {
    private final DawnAssets assets;
    private final HudSlotChrome chrome;
    private final Image bg;
    private final Image icon;
    private final Label countLabel;
    private final Table countWrap;
    private boolean selected;

    public HudItemSlot(DawnAssets assets, DawnFonts fonts, HudSlotChrome chrome) {
        this(assets, fonts, chrome, DawnTypography.SLOT_COUNT, HudSlotDesign.countPadPx());
    }

    public HudItemSlot(
            DawnAssets assets,
            DawnFonts fonts,
            HudSlotChrome chrome,
            DawnTypography.TextTier countTier,
            float countPadPx) {
        this.assets = assets;
        this.chrome = chrome;
        setTouchable(Touchable.disabled);

        if (chrome != HudSlotChrome.FLOATING) {
            bg = new Image();
        } else {
            bg = null;
        }
        icon = new Image();
        icon.setScaling(Scaling.stretch);
        icon.setAlign(Align.center);
        countLabel =
                DawnTypography.label(
                        "",
                        fonts,
                        DawnFonts.FontWeight.NORMAL,
                        countTier,
                        DawnTypography.TextContext.HUD,
                        Color.WHITE);
        countLabel.setAlignment(Align.bottom | Align.right);

        if (bg != null) {
            addActor(bg);
            applyBackground();
        }
        addActor(icon);
        countWrap = new Table();
        countWrap.setFillParent(true);
        countWrap.add(countLabel).expand().bottom().right().pad(countPadPx);
        addActor(countWrap);
    }

    public void setCountStyle(DawnFonts fonts, DawnTypography.TextTier countTier, float countPadPx) {
        countLabel.setStyle(
                new Label.LabelStyle(fonts.forTier(countTier), (Color) countLabel.getStyle().fontColor));
        countWrap.clearChildren();
        countWrap.add(countLabel).expand().bottom().right().pad(countPadPx);
    }

    public void setSelected(boolean selected) {
        if (chrome != HudSlotChrome.HOTBAR || this.selected == selected) {
            return;
        }
        this.selected = selected;
        applyBackground();
    }

    public void setLayoutSize(float slotPx, float iconPx) {
        setSize(slotPx, slotPx);
        if (bg != null) {
            bg.setSize(slotPx, slotPx);
            bg.setPosition(0f, 0f);
        }
        float ix = (slotPx - iconPx) / 2f;
        float iy = (slotPx - iconPx) / 2f;
        icon.setSize(iconPx, iconPx);
        icon.setPosition(ix, iy);
    }

    public void refresh(ItemStack stack) {
        refresh(stack, null);
    }

    /** {@code guideIcon} shown when stack is empty (equipment slot hints). */
    public void refresh(ItemStack stack, TextureRegion guideIcon) {
        if (stack == null || stack.isEmpty()) {
            if (guideIcon != null) {
                icon.setDrawable(new TextureRegionDrawable(guideIcon));
                icon.setVisible(true);
            } else {
                icon.setDrawable(null);
                icon.setVisible(false);
            }
            countLabel.setText("");
            return;
        }
        icon.setVisible(true);
        ItemDef def = ItemRegistry.get(stack);
        if (def != null && assets.item(def.iconId()) != null) {
            icon.setDrawable(new TextureRegionDrawable(assets.item(def.iconId())));
        } else {
            icon.setDrawable(null);
        }
        countLabel.setText(stack.count > 1 ? String.valueOf(stack.count) : "");
    }

    private void applyBackground() {
        if (bg == null) {
            return;
        }
        TextureRegion region =
                switch (chrome) {
                    case HOTBAR ->
                            selected ? assets.uiCommon.slotSelected : assets.uiCommon.slot;
                    case DULL -> assets.uiCommon.slotDull;
                    case FLOATING -> throw new IllegalStateException("floating slots have no background");
                };
        bg.setDrawable(new TextureRegionDrawable(region));
    }
}
