package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.entity.Entity;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityManager;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.item.ItemStack;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InteractionPresentationTest {
    private World world;
    private Entity player;
    private InteractionPresentation presentation;

    @BeforeEach
    void setUp() {
        world = TestWorlds.smallWalkable(8, 8);
        player = new EntityManager().spawn(EntityId.PLAYER, 4f, 4f);
        presentation = new InteractionPresentation();
    }

    @Test
    void placementPreviews_resolveEvenWhenGhostsHidden() {
        world.setGround(5, 4, BlockId.PIT);
        presentation.update(
                world,
                player,
                ItemStack.of(com.dawn.item.ItemId.STONE_GROUND),
                new TargetCell(5, 4, false),
                false);
        assertFalse(presentation.showPlacementGhosts());
        assertTrue(presentation.hasValidPlacementPreview());
    }
}
