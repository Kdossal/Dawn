package com.dawn.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.entity.Entity;
import com.dawn.entity.sprite.EntitySpriteFrame;
import com.dawn.gameplay.crafting.CraftingAffordability;
import com.dawn.gameplay.crafting.CraftingContextResolver;
import com.dawn.gameplay.crafting.CraftingSystem;
import com.dawn.gameplay.crafting.KnownRecipes;
import com.dawn.gameplay.crafting.Recipe;
import com.dawn.gameplay.crafting.RecipeContext;
import com.dawn.gameplay.crafting.RecipeId;
import com.dawn.inventory.PlayerInventory;
import com.dawn.inventory.PlayerProfile;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.ui.inventory.InventoryOverlay;
import java.util.ArrayList;
import java.util.List;

/** In-world crafting panel positioned via {@link WorldHudPopupPlacement}. */
public final class CraftingOverlay {
    private static final WorldHudPopupPlacement.Config PLACEMENT = WorldHudPopupPlacement.Config.crafting();

    private final DawnAssets assets;
    private final PlayerProfile profile;
    private final PlayerInventory inventory;
    private final CraftingSystem craftingSystem;
    private final Stage stage;
    private final HudItemDragSession dragSession;
    private final InventoryOverlay inventoryOverlay;

    private final Group root;
    private final Image panelBg;
    private final List<CraftingSlotWidget> slots = new ArrayList<>();
    private List<Recipe> recipes = List.of();

    private boolean open;
    private RecipeContext lastContext = RecipeContext.HAND;
    private ItemId lastHeldId;
    private ItemStack lastHeld = ItemStack.empty();

    public CraftingOverlay(
            DawnAssets assets,
            PlayerProfile profile,
            PlayerInventory inventory,
            CraftingSystem craftingSystem,
            Stage stage,
            HudItemDragSession dragSession,
            InventoryOverlay inventoryOverlay) {
        this.assets = assets;
        this.profile = profile;
        this.inventory = inventory;
        this.craftingSystem = craftingSystem;
        this.stage = stage;
        this.dragSession = dragSession;
        this.inventoryOverlay = inventoryOverlay;

        int mult = Constants.HUD_ART_MULT;
        NinePatch patch =
                new NinePatch(
                        assets.uiInventory.panel,
                        HudPanelDesign.BASE_NINE_SLICE,
                        HudPanelDesign.BASE_NINE_SLICE,
                        HudPanelDesign.BASE_NINE_SLICE,
                        HudPanelDesign.BASE_NINE_SLICE);
        patch.scale(mult, mult);
        panelBg = new Image(new NinePatchDrawable(patch));
        panelBg.setTouchable(Touchable.disabled);

        root = new Group();
        root.setVisible(false);
        root.setTouchable(Touchable.childrenOnly);
        root.addActor(panelBg);

        stage.addActor(root);
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        if (inventoryOverlay.isOpen()) {
            return;
        }
        if (open) {
            close();
        } else {
            open();
        }
    }

    public void open() {
        if (inventoryOverlay.isOpen()) {
            return;
        }
        open = true;
        root.setVisible(true);
        refresh(inventory.getHeld());
        root.toFront();
        dragSession.cursorActor().toFront();
    }

    public void close() {
        if (!open) {
            return;
        }
        open = false;
        root.setVisible(false);
        craftingSystem.onOverlayClosed();
    }

    public void refresh(ItemStack held) {
        lastHeld = held == null ? ItemStack.empty() : held;
        KnownRecipes known = profile.knownRecipes();
        recipes = CraftingContextResolver.recipesFor(lastHeld, known);
        syncSlotWidgets();
        refreshSlotChrome();
        lastContext = CraftingContextResolver.contextForHeld(lastHeld);
        lastHeldId = lastHeld.isEmpty() ? null : lastHeld.itemId;
    }

    private void refreshSlotChrome() {
        RecipeId selected = craftingSystem.selectedRecipe();
        RecipeId channeling = craftingSystem.channelingRecipe();
        float channelProgress = craftingSystem.channelProgressRatio();
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            boolean unavailable = !CraftingAffordability.canAfford(recipe, inventory);
            boolean selectedSlot = recipe.id() == selected;
            float progress = recipe.id() == channeling ? channelProgress : -1f;
            slots.get(i).refresh(recipe, unavailable, selectedSlot, progress);
        }
    }

    public void update(Entity player, OrthographicCamera worldCamera) {
        if (!open) {
            return;
        }
        ItemStack contextHeld = inventory.getHeld();
        RecipeContext context = CraftingContextResolver.contextForHeld(contextHeld);
        ItemId heldId = contextHeld.isEmpty() ? null : contextHeld.itemId;
        if (context != lastContext) {
            craftingSystem.cancelAll();
        }
        if (context != lastContext || heldId != lastHeldId) {
            refresh(contextHeld);
        }
        refreshSlotChrome();
        layoutAbovePlayer(player, worldCamera);
    }

    private void syncSlotWidgets() {
        while (slots.size() > recipes.size()) {
            CraftingSlotWidget removed = slots.remove(slots.size() - 1);
            removed.remove();
        }
        while (slots.size() < recipes.size()) {
            CraftingSlotWidget slot = new CraftingSlotWidget(assets);
            slot.setClickListener(
                    recipe -> {
                        if (!craftingSystem.isChanneling()) {
                            craftingSystem.onSlotClicked(recipe.id());
                            refreshSlotChrome();
                        }
                    });
            slots.add(slot);
            root.addActor(slot);
        }
        for (CraftingSlotWidget slot : slots) {
            slot.setVisible(true);
        }
    }

    private void layoutAbovePlayer(Entity player, OrthographicCamera worldCamera) {
        CraftingDesign.Layout layout = CraftingDesign.layout(recipes.size());

        EntitySpriteFrame frame = player.resolveSpriteFrame(assets);
        float spriteW =
                frame != null ? frame.widthPx() : (float) Constants.PLAYER_SPRITE_WIDTH_PX;
        float spriteH =
                frame != null ? frame.heightPx() : (float) Constants.PLAYER_SPRITE_HEIGHT_PX;

        WorldHudPopupPlacement.Anchor anchor =
                WorldHudPopupPlacement.spriteTopCenter(
                        player.getX(), player.getY(), spriteW, spriteH, worldCamera);
        WorldHudPopupPlacement.PanelPosition panel =
                WorldHudPopupPlacement.clampToStage(
                        WorldHudPopupPlacement.panelAboveAnchor(anchor, layout.panelW(), PLACEMENT),
                        layout.panelW(),
                        layout.panelH());

        root.setSize(layout.panelW(), layout.panelH());
        root.setPosition(panel.panelX(), panel.panelBottomY());

        panelBg.setSize(layout.panelW(), layout.panelH());
        panelBg.setPosition(0f, 0f);

        Rectangle slotBounds = new Rectangle();
        int idx = 0;
        for (int row = 0; row < layout.rows(); row++) {
            for (int col = 0; col < layout.cols(); col++) {
                if (idx >= layout.slotCount()) {
                    break;
                }
                CraftingDesign.slotBounds(0f, 0f, layout, col, row, slotBounds);
                CraftingSlotWidget slot = slots.get(idx++);
                slot.setPosition(slotBounds.x, slotBounds.y);
            }
        }
    }
}
