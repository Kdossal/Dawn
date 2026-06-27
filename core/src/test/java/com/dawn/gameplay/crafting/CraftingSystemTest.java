package com.dawn.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.gameplay.InteractionSystem;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.gameplay.drops.LootTable;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.test.TestWorlds;
import com.dawn.ui.inventory.InventoryCursorController;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CraftingSystemTest {
    private PlayerInventory inventory;
    private InventoryCursorController cursorController;
    private CraftingSystem craftingSystem;
    private World world;
    private Entity player;

    @BeforeEach
    void setUp() {
        inventory = new PlayerInventory();
        Entity entity = new Entity(EntityId.PLAYER, 10f, 10f);
        cursorController =
                new InventoryCursorController(
                        inventory, new EquipmentInventory(), new DropSystem(), entity, () -> {});
        craftingSystem = new CraftingSystem(inventory, cursorController, () -> {});
        world = World.createDefault();
        player = new Entity(EntityId.PLAYER, 50f, 50f);
    }

    @Test
    void grabChannelCompletesAndConsumes() {
        int clothBefore = CraftingAffordability.countInGrid(inventory, ItemId.CLOTH);
        craftingSystem.onSlotClicked(RecipeId.BANDAGE);
        assertTrue(craftingSystem.isGrabChanneling());
        craftingSystem.update(null, null, null, false, 3f, null);
        assertFalse(craftingSystem.isGrabChanneling());
        assertEquals(clothBefore - 2, CraftingAffordability.countInGrid(inventory, ItemId.CLOTH));
        assertTrue(cursorController.hasCraftCursor());
        assertEquals(1, cursorController.cursorStack().count);
    }

    @Test
    void grabChannelCancelDoesNotConsume() {
        int clothBefore = CraftingAffordability.countInGrid(inventory, ItemId.CLOTH);
        craftingSystem.onSlotClicked(RecipeId.BANDAGE);
        craftingSystem.update(null, null, null, false, 1f, null);
        craftingSystem.cancelAll();
        assertEquals(clothBefore, CraftingAffordability.countInGrid(inventory, ItemId.CLOTH));
        assertFalse(cursorController.hasCraftCursor());
    }

    @Test
    void unaffordableClickIsNoOp() {
        inventory.setSlot(1, 3, com.dawn.item.ItemStack.empty());
        craftingSystem.onSlotClicked(RecipeId.BANDAGE);
        assertFalse(craftingSystem.isGrabChanneling());
    }

    @Test
    void campfireClickEntersPlacementMode() {
        craftingSystem.onSlotClicked(RecipeId.CAMPFIRE);
        assertTrue(craftingSystem.isPlacementMode());
        assertEquals(RecipeId.CAMPFIRE, craftingSystem.selectedRecipe());
        assertFalse(craftingSystem.phantomHeld().isEmpty());
    }

    @Test
    void bandageClickShowsSelectedWhileChanneling() {
        craftingSystem.onSlotClicked(RecipeId.BANDAGE);
        assertTrue(craftingSystem.isGrabChanneling());
        assertEquals(RecipeId.BANDAGE, craftingSystem.selectedRecipe());
        assertEquals(RecipeId.BANDAGE, craftingSystem.channelingRecipe());
        assertEquals(0f, craftingSystem.channelProgressRatio(), 0.001f);
    }

    @Test
    void channelProgressAdvancesDuringGrab() {
        craftingSystem.onSlotClicked(RecipeId.BANDAGE);
        craftingSystem.update(null, null, null, false, 1.5f, null);
        assertEquals(0.5f, craftingSystem.channelProgressRatio(), 0.01f);
    }

    @Test
    void placementCancelDoesNotConsume() {
        int logsBefore = CraftingAffordability.countInGrid(inventory, ItemId.LOG);
        craftingSystem.onSlotClicked(RecipeId.CAMPFIRE);
        craftingSystem.cancelAll();
        assertFalse(craftingSystem.isPlacementMode());
        assertEquals(logsBefore, CraftingAffordability.countInGrid(inventory, ItemId.LOG));
    }

    @Test
    void placementChannelPlacesAndConsumes() {
        int x = 47;
        int y = 50;
        world.setObject(x, y, BlockId.AIR);
        InteractionSystem interaction = new InteractionSystem(new LootTable(), new DropSystem());
        craftingSystem.onSlotClicked(RecipeId.CAMPFIRE);
        TargetCell target = new TargetCell(x, y, false);
        int logsBefore = CraftingAffordability.countInGrid(inventory, ItemId.LOG);
        craftingSystem.update(world, player, target, true, 3f, interaction);
        assertEquals(BlockId.CAMPFIRE, world.getObject(x, y));
        assertEquals(logsBefore - 2, CraftingAffordability.countInGrid(inventory, ItemId.LOG));
        assertTrue(craftingSystem.isPlacementMode());
    }

    @Test
    void placementDeselectsWhenOutOfMaterials() {
        for (int i = 0; i < InventoryConstants.SIZE; i++) {
            inventory.setSlotAtIndex(i, ItemStack.empty());
        }
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.LOG, 2));
        int x = 47;
        int y = 50;
        world.setObject(x, y, BlockId.AIR);
        InteractionSystem interaction = new InteractionSystem(new LootTable(), new DropSystem());
        craftingSystem.onSlotClicked(RecipeId.CAMPFIRE);
        assertEquals(RecipeId.CAMPFIRE, craftingSystem.selectedRecipe());
        craftingSystem.update(world, player, new TargetCell(x, y, false), true, 3f, interaction);
        assertEquals(BlockId.CAMPFIRE, world.getObject(x, y));
        assertNull(craftingSystem.selectedRecipe());
        assertFalse(craftingSystem.isPlacementMode());
    }

    @Test
    void placementDeselectsWhenMaterialsLostWhileSelected() {
        craftingSystem.onSlotClicked(RecipeId.CAMPFIRE);
        for (int i = 0; i < InventoryConstants.SIZE; i++) {
            inventory.setSlotAtIndex(i, ItemStack.empty());
        }
        craftingSystem.update(world, player, null, false, 0f, null);
        assertNull(craftingSystem.selectedRecipe());
        assertFalse(craftingSystem.isPlacementMode());
    }

    @Test
    void bandageStacksOnRepeatedCraft() {
        craftingSystem.onSlotClicked(RecipeId.BANDAGE);
        craftingSystem.update(null, null, null, false, 3f, null);
        craftingSystem.onSlotClicked(RecipeId.BANDAGE);
        craftingSystem.update(null, null, null, false, 3f, null);
        assertEquals(2, cursorController.cursorStack().count);
    }

    @Test
    void lumberGrabConsumesLogAndProducesTwoOnCursor() {
        int logsBefore = CraftingAffordability.countInGrid(inventory, ItemId.LOG);
        craftingSystem.onSlotClicked(RecipeId.LUMBER);
        assertTrue(craftingSystem.isGrabChanneling());
        craftingSystem.update(null, null, null, false, 3f, null);
        assertFalse(craftingSystem.isGrabChanneling());
        assertEquals(logsBefore - 1, CraftingAffordability.countInGrid(inventory, ItemId.LOG));
        assertTrue(cursorController.hasCraftCursor());
        assertEquals(ItemId.LUMBER, cursorController.cursorStack().itemId);
        assertEquals(2, cursorController.cursorStack().count);
    }

    @Test
    void dirtGroundPlacementInPitConsumesDirt() {
        world = TestWorlds.smallClear(64, 64);
        int x = 47;
        int y = 50;
        inventory.setSlotAtIndex(0, ItemStack.of(ItemId.DIRT, 4));
        InteractionSystem interaction = new InteractionSystem(new LootTable(), new DropSystem());
        craftingSystem.onSlotClicked(RecipeId.DIRT_GROUND);
        assertTrue(craftingSystem.isPlacementMode());
        int dirtBefore = CraftingAffordability.countInGrid(inventory, ItemId.DIRT);
        craftingSystem.update(world, player, new TargetCell(x, y, false), true, 3f, interaction);
        assertEquals(BlockId.DIRT_GROUND, world.getGround(x, y));
        assertEquals(dirtBefore - 1, CraftingAffordability.countInGrid(inventory, ItemId.DIRT));
    }

    @Test
    void placementClearsWhenContextChanges() {
        craftingSystem.onSlotClicked(RecipeId.CAMPFIRE);
        assertTrue(craftingSystem.isPlacementMode());
        assertEquals(RecipeId.CAMPFIRE, craftingSystem.selectedRecipe());
        craftingSystem.cancelAll();
        assertFalse(craftingSystem.isPlacementMode());
        assertNull(craftingSystem.selectedRecipe());
        assertTrue(craftingSystem.phantomHeld().isEmpty());
    }
}
