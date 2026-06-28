package com.dawn.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.gameplay.crafting.CraftingAffordability;
import com.dawn.gameplay.crafting.CraftingContextResolver;
import com.dawn.gameplay.crafting.KnownRecipes;
import com.dawn.gameplay.crafting.Recipe;
import com.dawn.gameplay.crafting.RecipeContext;
import com.dawn.gameplay.crafting.RecipeId;
import com.dawn.gameplay.crafting.RecipeRegistry;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import org.junit.jupiter.api.Test;

class CraftingOverlayTest {
    @Test
    void handContext_showsTwoRecipeSlots() {
        int count =
                CraftingContextResolver.recipesFor(ItemStack.empty(), new KnownRecipes()).size();
        assertEquals(2, count);
        assertEquals(2, CraftingDesign.layout(count).slotCount());
    }

    @Test
    void hammerContext_showsEmptyPanelFootprint() {
        int count =
                CraftingContextResolver.recipesFor(ItemStack.of(ItemId.HAMMER), new KnownRecipes())
                        .size();
        assertEquals(0, count);
        assertEquals(0, CraftingDesign.layout(count).slotCount());
        assertEquals(3, CraftingDesign.layout(count).cols());
    }

    @Test
    void clickHint_inventoryHiddenWhenInteractShown() {
        assertFalse(ClickHintRenderer.showInventoryKeyHint(true, true));
        assertTrue(ClickHintRenderer.showInventoryKeyHint(true, false));
        assertFalse(ClickHintRenderer.showInventoryKeyHint(false, false));
    }

    @Test
    void slotChrome_unavailableWhenMissingMaterials() {
        PlayerInventory inv = new PlayerInventory();
        inv.setSlot(1, 3, ItemStack.empty());
        Recipe bandage = RecipeRegistry.get(RecipeId.BANDAGE);
        assertFalse(CraftingAffordability.canAfford(bandage, inv));
    }

    @Test
    void recipeContextUsesHotbarSelectionNotCraftCursor() {
        PlayerInventory inventory = new PlayerInventory();
        inventory.setSelectedIndex(11);
        ItemStack hotbarHeld = inventory.getHeld();
        ItemStack craftCursor = ItemStack.of(ItemId.LUMBER, 2);

        assertEquals(RecipeContext.SAW, CraftingContextResolver.contextForHeld(hotbarHeld));
        assertEquals(RecipeContext.HAND, CraftingContextResolver.contextForHeld(craftCursor));
    }

    @Test
    void slotChrome_selectedMatchesRecipeId() {
        RecipeId selected = RecipeId.CAMPFIRE;
        Recipe campfire = RecipeRegistry.get(RecipeId.CAMPFIRE);
        assertTrue(campfire.id() == selected);
    }
}
