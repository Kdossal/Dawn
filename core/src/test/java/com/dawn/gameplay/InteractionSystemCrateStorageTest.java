package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.gameplay.drops.LootTable;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;
import com.dawn.item.PlaceableExecutor;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import com.dawn.world.storage.CrateStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InteractionSystemCrateStorageTest {
    private World world;
    private DropSystem dropSystem;
    private InteractionSystem interaction;
    private PlayerInventory inventory;
    private Entity player;

    @BeforeEach
    void setUp() {
        world = World.createDefault();
        dropSystem = new DropSystem();
        interaction = new InteractionSystem(new LootTable(), dropSystem);
        inventory = new PlayerInventory();
        player = new Entity(EntityId.PLAYER, 10f, 10f);
    }

    @Test
    void placeCrateObject_withStorageBootstrap_hasStorage() {
        int x = 12;
        int y = 10;
        world.setGround(x, y, BlockId.STONE_GROUND);
        world.setObject(x, y, BlockId.AIR);

        assertTrue(PlaceableExecutor.apply(world, player, new Placeable.Block(BlockId.CRATE), x, y));
        world.getCrateStorage().createAt(x, y);

        assertTrue(world.getCrateStorage().hasAt(x, y));
    }

    @Test
    void tryPlace_lantern_doesNotCreateStorage() {
        int x = 12;
        int y = 10;
        world.setGround(x, y, BlockId.STONE_GROUND);
        world.setObject(x, y, BlockId.AIR);

        ItemStack held = ItemStack.of(ItemId.LANTERN, 1);
        boolean placed =
                interaction.tryPlace(
                        world,
                        player,
                        inventory,
                        player.getX(),
                        player.getY(),
                        x,
                        y,
                        held,
                        amount -> ItemStack.of(ItemId.LANTERN, amount));

        assertTrue(placed);
        assertFalse(world.getCrateStorage().hasAt(x, y));
    }

    @Test
    void breakObject_crate_removesStorage() {
        int x = 5;
        int y = 5;
        world.setObject(x, y, BlockId.CRATE);
        world.getCrateStorage().createAt(x, y);

        interaction.executeBreak(world, x, y, Layer.OBJECT, BlockId.CRATE);

        assertNull(world.getCrateStorage().getAt(x, y));
        assertEqualsAir(world.getObject(x, y));
    }

    @Test
    void breakObject_crate_spillsStoredContents() {
        int x = 5;
        int y = 5;
        world.setObject(x, y, BlockId.CRATE);
        CrateStorage storage = world.getCrateStorage().createAt(x, y);
        storage.setSlotAtIndex(0, ItemStack.of(ItemId.SAND, 3));
        storage.setSlotAtIndex(4, ItemStack.of(ItemId.CLOTH, 1));

        interaction.executeBreak(world, x, y, Layer.OBJECT, BlockId.CRATE);

        assertEquals(3, totalCount(ItemId.SAND));
        assertEquals(1, totalCount(ItemId.CLOTH));
        assertTrue(interaction.getLastMessage().contains("contents spilled"));
    }

    @Test
    void breakObject_crate_emptyStorage_onlyLootTableDrops() {
        int x = 5;
        int y = 5;
        world.setObject(x, y, BlockId.CRATE);
        world.getCrateStorage().createAt(x, y);

        interaction.executeBreak(world, x, y, Layer.OBJECT, BlockId.CRATE);

        assertEquals(1, totalCount(ItemId.LUMBER));
        assertEquals(0, totalCount(ItemId.SAND));
        assertFalse(interaction.getLastMessage().contains("contents spilled"));
    }

    private int totalCount(ItemId itemId) {
        return dropSystem.getDrops().stream()
                .filter(d -> d.stack.itemId == itemId)
                .mapToInt(d -> d.stack.count)
                .sum();
    }

    private static void assertEqualsAir(BlockId id) {
        org.junit.jupiter.api.Assertions.assertEquals(BlockId.AIR, id);
    }
}
