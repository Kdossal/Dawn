package com.dawn.entity.sprite;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.gameplay.TargetResolver.TargetCell;
import org.junit.jupiter.api.Test;

class EntityAnimResolverTest {

    @Test
    void selectClip_idleWhenStill_usesIdleClip() {
        var ctx = new PlayerAnimContext(false, false, 4f, 4f, 0f, 0f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing2.LEFT);
        assertEquals("idle", selection.clipId());
        assertEquals(Facing2.LEFT, selection.facing());
        assertFalse(selection.facing().flipX());
    }

    @Test
    void selectClip_idleKeepsLastFacing() {
        var ctx = new PlayerAnimContext(false, false, 4f, 4f, 0f, 0f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing2.RIGHT);
        assertEquals("idle", selection.clipId());
        assertEquals(Facing2.RIGHT, selection.facing());
        assertTrue(selection.facing().flipX());
    }

    @Test
    void selectClip_verticalMove_keepsLastFacing() {
        var ctx = new PlayerAnimContext(true, false, 4f, 4f, 0f, -1f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing2.LEFT);
        assertEquals("walk", selection.clipId());
        assertEquals(Facing2.LEFT, selection.facing());
        assertFalse(selection.facing().flipX());
    }

    @Test
    void selectClip_walkLeftUsesUnflippedArt() {
        var ctx = new PlayerAnimContext(true, false, 4f, 4f, -1f, 0f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing2.RIGHT);
        assertEquals("walk", selection.clipId());
        assertEquals(Facing2.LEFT, selection.facing());
        assertFalse(selection.facing().flipX());
    }

    @Test
    void selectClip_walkRightFlipsArt() {
        var ctx = new PlayerAnimContext(true, false, 4f, 4f, 1f, 0f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing2.LEFT);
        assertEquals("walk", selection.clipId());
        assertEquals(Facing2.RIGHT, selection.facing());
        assertTrue(selection.facing().flipX());
    }

    @Test
    void selectClip_interactBeatsWalk() {
        var ctx = new PlayerAnimContext(true, true, 4.5f, 4.5f, 0f, 1f, new TargetCell(4, 5, false));
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing2.LEFT);
        assertEquals("interact", selection.clipId());
        assertEquals(Facing2.LEFT, selection.facing());
    }

    @Test
    void selectClip_interactWithoutTarget_keepsCurrentFacing() {
        var ctx = new PlayerAnimContext(true, true, 4f, 4f, 0f, 1f, null);
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, Facing2.RIGHT);
        assertEquals("interact", selection.clipId());
        assertEquals(Facing2.RIGHT, selection.facing());
        assertTrue(selection.facing().flipX());
    }

    @Test
    void facingToward_horizontalTarget() {
        Facing2 facing = EntityAnimResolver.facingToward(4f, 4f, new TargetCell(6, 4, false), Facing2.LEFT);
        assertEquals(Facing2.RIGHT, facing);
        assertTrue(facing.flipX());
    }

    @Test
    void facingToward_verticalTarget_keepsCurrentFacing() {
        Facing2 facing = EntityAnimResolver.facingToward(4.5f, 4.5f, new TargetCell(4, 6, false), Facing2.LEFT);
        assertEquals(Facing2.LEFT, facing);
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
        assertEquals("base", def.spriteId());
        assertEquals(3, def.rows());
        assertEquals(4, def.cols());
        assertEquals(32, def.frameWidth());
        assertEquals(48, def.frameHeight());
    }
}
