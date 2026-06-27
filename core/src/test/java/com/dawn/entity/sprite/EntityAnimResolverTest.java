package com.dawn.entity.sprite;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.gameplay.TargetResolver.TargetCell;
import org.junit.jupiter.api.Test;

class EntityAnimResolverTest {

    @Test
    void selectClip_idleWhenStill_usesDirectionalIdle() {
        var ctx = new PlayerAnimContext(false, false, 4f, 4f, 0f, 0f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing4.DOWN);
        assertEquals("idle_down", selection.clipId());
        assertEquals(Facing4.DOWN, selection.facing());
    }

    @Test
    void selectClip_idleKeepsLastFacing() {
        var ctx = new PlayerAnimContext(false, false, 4f, 4f, 0f, 0f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing4.LEFT);
        assertEquals("idle_right", selection.clipId());
        assertEquals(Facing4.LEFT, selection.facing());
        assertTrue(selection.facing().flipX());
    }

    @Test
    void selectClip_walkDownFromMoveVector() {
        var ctx = new PlayerAnimContext(true, false, 4f, 4f, 0f, -1f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing4.RIGHT);
        assertEquals("walk_down", selection.clipId());
        assertEquals(Facing4.DOWN, selection.facing());
    }

    @Test
    void selectClip_walkLeftUsesRightClipAndFlip() {
        var ctx = new PlayerAnimContext(true, false, 4f, 4f, -1f, 0f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing4.DOWN);
        assertEquals("walk_right", selection.clipId());
        assertEquals(Facing4.LEFT, selection.facing());
        assertTrue(selection.facing().flipX());
    }

    @Test
    void selectClip_interactBeatsWalk() {
        var ctx = new PlayerAnimContext(true, true, 4f, 4f, 0f, 1f, new TargetCell(4, 5, false));
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing4.DOWN);
        assertEquals("interact_up", selection.clipId());
    }

    @Test
    void selectClip_interactWithoutTarget_keepsCurrentFacing() {
        var ctx = new PlayerAnimContext(true, true, 4f, 4f, 0f, 1f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing4.LEFT);
        assertEquals("interact_right", selection.clipId());
        assertEquals(Facing4.LEFT, selection.facing());
        assertTrue(selection.facing().flipX());
    }

    @Test
    void facingToward_targetCellDominantAxis() {
        Facing4 facing = EntityAnimResolver.facingToward(4f, 4f, new TargetCell(6, 4, false));
        assertEquals(Facing4.RIGHT, facing);
        facing = EntityAnimResolver.facingToward(4f, 4f, new TargetCell(4, 6, false));
        assertEquals(Facing4.UP, facing);
    }

    @Test
    void frameIndex_wrapsWithTime() {
        EntityAnimClip clip = new EntityAnimClip(1, 4, 8f);
        assertEquals(0, EntityAnimResolver.frameIndex(clip, 0f));
        assertEquals(2, EntityAnimResolver.frameIndex(clip, 0.3125f));
        assertEquals(0, EntityAnimResolver.frameIndex(clip, 0.5f));
    }

    @Test
    void load_playerAnimDefPresent() {
        var defs = EntityAnimDefinitionsLoader.load();
        EntityAnimDef def = defs.get(com.dawn.entity.EntityId.PLAYER);
        assertNotNull(def);
        assertEquals(9, def.rows());
        assertEquals(4, def.cols());
        assertEquals(25, def.frameHeight());
    }
}
