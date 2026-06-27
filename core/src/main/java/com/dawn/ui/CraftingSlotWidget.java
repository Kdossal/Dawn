package com.dawn.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.dawn.assets.DawnAssets;
import com.dawn.gameplay.crafting.Recipe;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import java.util.function.Consumer;

/** Crafting recipe slot — base chrome, icon, unavailable/selected/time overlays on top. */
public final class CraftingSlotWidget extends Group {
    private final TextureRegionDrawable timeDrawable;
    private final DawnAssets assets;
    private final Image chrome;
    private final Image icon;
    private final Image unavailableOverlay;
    private final Image selectedOverlay;
    private final Image timeOverlay;
    private Recipe recipe;
    private Consumer<Recipe> clickListener;

    public CraftingSlotWidget(DawnAssets assets) {
        this.assets = assets;
        setTouchable(Touchable.enabled);
        addListener(
                new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (recipe != null && clickListener != null) {
                            clickListener.accept(recipe);
                        }
                    }
                });

        CraftingSlotDesign.ChromeRegions regions =
                CraftingSlotDesign.chromeRegions(assets.uiCommon.craftingSlot);
        chrome = new Image(new TextureRegionDrawable(regions.slotBase()));
        unavailableOverlay = new Image(new TextureRegionDrawable(regions.unavailable()));
        selectedOverlay = new Image(new TextureRegionDrawable(regions.selected()));
        timeDrawable = new TextureRegionDrawable(regions.time());
        timeOverlay = new Image(timeDrawable);
        unavailableOverlay.setVisible(false);
        selectedOverlay.setVisible(false);
        timeOverlay.setVisible(false);
        icon = new Image();
        icon.setScaling(Scaling.stretch);
        icon.setAlign(Align.center);

        addActor(chrome);
        addActor(icon);
        addActor(unavailableOverlay);
        addActor(selectedOverlay);
        addActor(timeOverlay);
        applyLayout();
    }

    public void setClickListener(Consumer<Recipe> clickListener) {
        this.clickListener = clickListener;
    }

    boolean isUnavailableOverlayVisible() {
        return unavailableOverlay.isVisible();
    }

    boolean isSelectedOverlayVisible() {
        return selectedOverlay.isVisible();
    }

    boolean isTimeOverlayVisible() {
        return timeOverlay.isVisible();
    }

    private void applyLayout() {
        float cellPx = CraftingSlotDesign.cellPx();
        float iconPx = SlotUi.iconPxForSlot(cellPx);

        setSize(cellPx, cellPx);
        chrome.setSize(cellPx, cellPx);
        chrome.setPosition(0f, 0f);
        unavailableOverlay.setSize(cellPx, cellPx);
        unavailableOverlay.setPosition(0f, 0f);
        selectedOverlay.setSize(cellPx, cellPx);
        selectedOverlay.setPosition(0f, 0f);

        float ix = (cellPx - iconPx) / 2f;
        float iy = (cellPx - iconPx) / 2f;
        icon.setSize(iconPx, iconPx);
        icon.setPosition(ix, iy);

        timeOverlay.setOrigin(0f, 0f);
        layoutTimeOverlay(-1f);
    }

    /**
     * @param channelProgress 0..1 while channeling this recipe; negative when time overlay hidden
     */
    public void refresh(Recipe recipe, boolean unavailable, boolean selected, float channelProgress) {
        this.recipe = recipe;
        unavailableOverlay.setVisible(unavailable);
        selectedOverlay.setVisible(selected);
        layoutTimeOverlay(channelProgress);
        if (recipe == null) {
            icon.setDrawable(null);
            icon.setVisible(false);
            return;
        }
        ItemDef def = ItemRegistry.get(recipe.iconItemId());
        if (def != null && assets.item(def.iconId()) != null) {
            icon.setDrawable(new TextureRegionDrawable(assets.item(def.iconId())));
            icon.setVisible(true);
        } else {
            icon.setDrawable(null);
            icon.setVisible(false);
        }
    }

    private void layoutTimeOverlay(float channelProgress) {
        float cellPx = CraftingSlotDesign.cellPx();
        if (channelProgress < 0f) {
            timeOverlay.setVisible(false);
            return;
        }
        float clamped = Math.min(1f, Math.max(0f, channelProgress));
        float cover = 1f - clamped;
        if (cover <= 0.001f) {
            timeOverlay.setVisible(false);
            return;
        }
        float h = cellPx * cover;
        timeOverlay.setVisible(true);
        timeOverlay.setSize(cellPx, h);
        timeOverlay.setPosition(0f, 0f);
    }
}
