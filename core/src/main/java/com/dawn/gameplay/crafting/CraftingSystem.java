package com.dawn.gameplay.crafting;

import com.dawn.entity.Entity;
import com.dawn.gameplay.InteractionSystem;
import com.dawn.gameplay.PlacementRules;
import com.dawn.gameplay.ReachResolver;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;
import com.dawn.item.PlaceableExecutor;
import com.dawn.ui.inventory.InventoryCursorController;
import com.dawn.world.World;

/** World crafting execution: grab channel, placement mode, and placement channel. */
public final class CraftingSystem {
    private enum State {
        IDLE,
        GRAB_CHANNELING,
        PLACEMENT_MODE,
        PLACE_CHANNELING
    }

    private final PlayerInventory inventory;
    private final InventoryCursorController cursorController;
    private final Runnable onChanged;

    private State state = State.IDLE;
    private RecipeId activeRecipeId;
    private float channelProgressSec;
    private int placeChannelX;
    private int placeChannelY;

    public CraftingSystem(
            PlayerInventory inventory, InventoryCursorController cursorController, Runnable onChanged) {
        this.inventory = inventory;
        this.cursorController = cursorController;
        this.onChanged = onChanged;
    }

    public void onSlotClicked(RecipeId recipeId) {
        if (recipeId == null || isChanneling()) {
            return;
        }
        Recipe recipe = RecipeRegistry.get(recipeId);
        if (recipe == null || !CraftingAffordability.canAfford(recipe, inventory)) {
            return;
        }
        if (recipe.result() instanceof RecipeResult.Grab) {
            if (state == State.PLACEMENT_MODE || state == State.PLACE_CHANNELING) {
                exitPlacementMode();
            }
            activeRecipeId = recipeId;
            channelProgressSec = 0f;
            state = State.GRAB_CHANNELING;
            return;
        }
        if (recipe.result() instanceof RecipeResult.Place) {
            if (state == State.GRAB_CHANNELING) {
                return;
            }
            activeRecipeId = recipeId;
            channelProgressSec = 0f;
            placeChannelX = 0;
            placeChannelY = 0;
            state = State.PLACEMENT_MODE;
        }
    }

    public void update(
            World world,
            Entity player,
            TargetCell target,
            boolean placeHeld,
            float delta,
            InteractionSystem interaction) {
        switch (state) {
            case GRAB_CHANNELING -> updateGrabChannel(delta);
            case PLACEMENT_MODE, PLACE_CHANNELING -> updatePlacement(world, player, target, placeHeld, delta, interaction);
            default -> {}
        }
    }

    private void updateGrabChannel(float delta) {
        Recipe recipe = RecipeRegistry.get(activeRecipeId);
        if (recipe == null) {
            resetToIdle();
            return;
        }
        if (!CraftingAffordability.canAfford(recipe, inventory)) {
            resetToIdle();
            return;
        }
        channelProgressSec += delta;
        if (channelProgressSec < recipe.craftTimeSec()) {
            return;
        }
        if (!CraftingAffordability.consumeCosts(recipe, inventory)) {
            resetToIdle();
            return;
        }
        RecipeResult.Grab grab = (RecipeResult.Grab) recipe.result();
        cursorController.receiveCraftedStack(grab.stack());
        notifyChanged();
        resetToIdle();
    }

    private void updatePlacement(
            World world,
            Entity player,
            TargetCell target,
            boolean placeHeld,
            float delta,
            InteractionSystem interaction) {
        Recipe recipe = RecipeRegistry.get(activeRecipeId);
        if (recipe == null || !(recipe.result() instanceof RecipeResult.Place placeResult)) {
            exitPlacementMode();
            return;
        }
        if (!CraftingAffordability.canAfford(recipe, inventory)) {
            exitPlacementMode();
            notifyChanged();
            return;
        }
        if (!placeHeld || target == null) {
            if (state == State.PLACE_CHANNELING) {
                state = State.PLACEMENT_MODE;
                channelProgressSec = 0f;
            }
            return;
        }

        PlacementRules.Result rules = evaluatePlacement(world, player, target.x(), target.y());
        if (rules == null || !rules.valid()) {
            if (state == State.PLACE_CHANNELING) {
                state = State.PLACEMENT_MODE;
                channelProgressSec = 0f;
            }
            return;
        }

        if (state == State.PLACE_CHANNELING
                && (placeChannelX != target.x() || placeChannelY != target.y())) {
            state = State.PLACEMENT_MODE;
            channelProgressSec = 0f;
        }

        if (state == State.PLACEMENT_MODE) {
            state = State.PLACE_CHANNELING;
            placeChannelX = target.x();
            placeChannelY = target.y();
            channelProgressSec = 0f;
        }

        channelProgressSec += delta;
        if (channelProgressSec < recipe.craftTimeSec()) {
            return;
        }

        if (!CraftingAffordability.canAfford(recipe, inventory)) {
            exitPlacementMode();
            notifyChanged();
            return;
        }

        if (!PlaceableExecutor.apply(world, player, rules.placeable(), rules.anchorX(), rules.anchorY())) {
            state = State.PLACEMENT_MODE;
            channelProgressSec = 0f;
            if (interaction != null) {
                interaction.setMessage(PlaceableExecutor.placementError(placeResult.placeable()));
            }
            return;
        }

        if (!CraftingAffordability.consumeCosts(recipe, inventory)) {
            rollbackPlacement(world, rules.placeable(), rules.anchorX(), rules.anchorY());
            exitPlacementMode();
            notifyChanged();
            return;
        }

        if (interaction != null) {
            interaction.setMessage("Placed " + recipe.displayName().toLowerCase());
        }
        notifyChanged();
        if (CraftingAffordability.canAfford(recipe, inventory)) {
            state = State.PLACEMENT_MODE;
            channelProgressSec = 0f;
        } else {
            exitPlacementMode();
        }
    }

    private static void rollbackPlacement(World world, Placeable placeable, int x, int y) {
        if (placeable instanceof Placeable.Block) {
            world.setObject(x, y, com.dawn.world.block.BlockId.AIR);
        } else if (placeable instanceof Placeable.Ground ground) {
            world.setGround(x, y, com.dawn.world.block.BlockId.PIT);
        }
    }

    public void cancelAll() {
        if (state == State.GRAB_CHANNELING || state == State.PLACE_CHANNELING) {
            resetToIdle();
            return;
        }
        if (state == State.PLACEMENT_MODE) {
            exitPlacementMode();
        }
    }

    public void onOverlayClosed() {
        cancelAll();
        if (cursorController.hasCraftCursor()) {
            cursorController.dropCraftCursorToWorld();
        }
    }

    public boolean isGrabChanneling() {
        return state == State.GRAB_CHANNELING;
    }

    public boolean isPlacementMode() {
        return state == State.PLACEMENT_MODE || state == State.PLACE_CHANNELING;
    }

    public boolean isPlaceChanneling() {
        return state == State.PLACE_CHANNELING;
    }

    public boolean isChanneling() {
        return state == State.GRAB_CHANNELING || state == State.PLACE_CHANNELING;
    }

    public boolean isInteracting() {
        return isChanneling();
    }

    public RecipeId selectedRecipe() {
        return state == State.IDLE ? null : activeRecipeId;
    }

    public RecipeId channelingRecipe() {
        return isChanneling() ? activeRecipeId : null;
    }

    /** 0 at channel start, 1 when complete. */
    public float channelProgressRatio() {
        if (!isChanneling() || activeRecipeId == null) {
            return 0f;
        }
        Recipe recipe = RecipeRegistry.get(activeRecipeId);
        if (recipe == null || recipe.craftTimeSec() <= 0f) {
            return 0f;
        }
        return Math.min(1f, channelProgressSec / recipe.craftTimeSec());
    }

    public ItemStack phantomHeld() {
        if (!isPlacementMode()) {
            return ItemStack.empty();
        }
        Recipe recipe = RecipeRegistry.get(activeRecipeId);
        if (recipe == null) {
            return ItemStack.empty();
        }
        return ItemStack.of(recipe.iconItemId(), 1);
    }

    public Placeable activePlaceable() {
        if (!isPlacementMode()) {
            return null;
        }
        Recipe recipe = RecipeRegistry.get(activeRecipeId);
        if (recipe == null || !(recipe.result() instanceof RecipeResult.Place place)) {
            return null;
        }
        return place.placeable();
    }

    public boolean hasValidPlacementPreview(World world, Entity player, TargetCell target) {
        if (!isPlacementMode() || target == null) {
            return false;
        }
        PlacementRules.Result rules = evaluatePlacement(world, player, target.x(), target.y());
        return rules != null && rules.valid();
    }

    private PlacementRules.Result evaluatePlacement(World world, Entity player, int cellX, int cellY) {
        Placeable placeable = activePlaceable();
        if (placeable == null) {
            return null;
        }
        return PlacementRules.evaluate(
                world,
                player,
                player.getX(),
                player.getY(),
                placeable,
                ReachResolver.radiusCellsFloatForHeld(phantomHeld()),
                cellX,
                cellY);
    }

    private void exitPlacementMode() {
        resetToIdle();
    }

    private void resetToIdle() {
        state = State.IDLE;
        activeRecipeId = null;
        channelProgressSec = 0f;
        placeChannelX = 0;
        placeChannelY = 0;
    }

    private void notifyChanged() {
        if (onChanged != null) {
            onChanged.run();
        }
    }
}
