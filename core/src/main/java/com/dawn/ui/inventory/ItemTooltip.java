package com.dawn.ui.inventory;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.dawn.assets.DawnAssets;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.ui.DawnFonts;
import com.dawn.ui.DawnTypography;
import com.dawn.ui.DawnTypography.TextContext;
import com.dawn.ui.DawnTypography.TextTier;

public final class ItemTooltip {
    private final Table tooltip;
    private final Label textLabel;

    public ItemTooltip(DawnAssets assets, DawnFonts fonts) {
        tooltip = new Table();
        tooltip.setBackground(InventoryUiStyle.tooltipBackground(assets));
        tooltip.pad(2f, 4f, 2f, 4f);
        textLabel = DawnTypography.label(
                "", fonts, DawnFonts.FontWeight.NORMAL, TextTier.SM, TextContext.HUD, InventoryUiStyle.LABEL_COLOR);
        textLabel.setAlignment(Align.center);
        tooltip.add(textLabel);
        tooltip.setVisible(false);
        tooltip.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    public void attach(Stage stage) {
        stage.addActor(tooltip);
    }

    public void bind(Actor slot, java.util.function.Supplier<ItemStack> stackSupplier) {
        slot.addListener(
                new InputListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        ItemStack stack = stackSupplier.get();
                        if (stack == null || stack.isEmpty()) {
                            hide();
                            return;
                        }
                        ItemDef def = ItemRegistry.get(stack);
                        if (def == null) {
                            hide();
                            return;
                        }
                        String hint = def.equipmentSlot() != null
                                ? def.displayName() + "\n(" + def.equipmentSlot() + ")"
                                : def.displayName();
                        show(slot, hint);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        hide();
                    }
                });
    }

    private void show(Actor slot, String text) {
        textLabel.setText(text);
        tooltip.pack();
        tooltip.setVisible(true);
        Vector2 topCenter = slot.localToStageCoordinates(new Vector2(slot.getWidth() / 2f, slot.getHeight()));
        tooltip.setPosition(topCenter.x - tooltip.getWidth() / 2f, topCenter.y + 6f);
    }

    private void hide() {
        tooltip.setVisible(false);
    }
}
