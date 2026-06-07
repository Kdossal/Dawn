package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.config.GameConfig;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.gameplay.drops.LootTable;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemStack;
import com.dawn.test.TestWorlds;
import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityManager;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlacementSystemTest {
    private World world;
    private Entity player;
    private PlayerInventory inventory;
    private PlacementSystem placement;
    private float savedPulseSec;

    @BeforeEach
    void setUp() {
        savedPulseSec = GameConfig.get().placeInteractPulseSec;
        GameConfig.get().placeInteractPulseSec = 0.12f;
        world = TestWorlds.smallWalkable(8, 8);
        player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
        inventory = new PlayerInventory();
        placement = new PlacementSystem(new InteractionSystem(new LootTable(), new DropSystem()));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        GameConfig.get().placeInteractPulseSec = savedPulseSec;
    }

    @Test
    void successfulPlace_startsInteractPulse() {
        world.setGround(5, 4, BlockId.PIT);
        TargetCell target = new TargetCell(5, 4, false);
        placement.update(world, player, inventory, target, ItemStack.of(com.dawn.item.ItemId.DIRT_CLUMP), true, 0f);
        assertTrue(placement.isInteracting());
    }

    @Test
    void interactPulse_decaysOverTime() {
        world.setGround(5, 4, BlockId.PIT);
        TargetCell target = new TargetCell(5, 4, false);
        placement.update(world, player, inventory, target, ItemStack.of(com.dawn.item.ItemId.DIRT_CLUMP), true, 0f);
        placement.tick(0.13f);
        assertFalse(placement.isInteracting());
    }
}
