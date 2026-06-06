package com.dawn.world.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.entity.EntityBounds;
import com.dawn.entity.EntityDef;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityRegistry;
import com.dawn.render.SpriteAlphaMask;
import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.visual.BlockVisualLayout;
import com.dawn.world.block.visual.BlockVisualRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class OcclusionFadeTest {
    private static final EntityDef PLAYER = EntityRegistry.get(EntityId.PLAYER);

    @Test
    void movementBoxBehind_overlapsAndNorthOfSpriteBottom() {
        float[] footprint =
                com.dawn.world.block.visual.BlockVisualLayout.spriteFootprintCell(
                        BlockVisualRegistry.get(BlockId.BUSH), 4, 7);
        EntityBounds player = EntityBounds.fromFeet(PLAYER, 4.5f, 7.2f, 32, 64);
        assertTrue(OcclusionFade.movementBoxBehindFootprint(
                player, footprint[0], footprint[1], footprint[2], footprint[3]));
    }

    @Test
    void movementBoxNotBehind_whenOnlyHigherY_noHorizontalOverlap() {
        float[] footprint =
                com.dawn.world.block.visual.BlockVisualLayout.spriteFootprintCell(
                        BlockVisualRegistry.get(BlockId.OAK_TREE), 4, 5);
        EntityBounds player = EntityBounds.fromFeet(PLAYER, 10f, 8f, 32, 64);
        assertFalse(OcclusionFade.movementBoxBehindFootprint(
                player, footprint[0], footprint[1], footprint[2], footprint[3]));
    }

    @Test
    void movementBoxNotBehind_whenSouthOfSprite() {
        float[] footprint =
                com.dawn.world.block.visual.BlockVisualLayout.spriteFootprintCell(
                        BlockVisualRegistry.get(BlockId.OAK_TREE), 4, 5);
        EntityBounds player = EntityBounds.fromFeet(PLAYER, 4.5f, 4f, 32, 64);
        assertFalse(OcclusionFade.movementBoxBehindFootprint(
                player, footprint[0], footprint[1], footprint[2], footprint[3]));
    }

    @Test
    void opaqueOverlap_requiresBothSpritesOpaque() {
        SpriteAlphaMask solid2x2 = solidRect(2, 2);
        SpriteAlphaMask hollow =
                SpriteAlphaMask.of(
                        4,
                        4,
                        new boolean[] {
                            false, false, false, false,
                            false, true, true, false,
                            false, true, true, false,
                            false, false, false, false
                        });

        assertTrue(SpriteAlphaMask.opaqueOverlap(solid2x2, 0f, 0f, solid2x2, 1f, 1f));
        assertFalse(SpriteAlphaMask.opaqueOverlap(solid2x2, 0f, 0f, hollow, 3f, 0f));
    }

    @Test
    void playerBehindOccluder_falseWhenAabbOverlapButNoOpaquePixels() {
        SpriteAlphaMask player = solidRect(32, 64);
        SpriteAlphaMask tree =
                SpriteAlphaMask.of(
                        48,
                        64,
                        opaqueColumn(48, 64, 47));
        var visual = BlockVisualRegistry.get(BlockId.OAK_TREE);
        EntityBounds bounds = EntityBounds.fromFeet(PLAYER, 4.5f, 7.2f, 32, 64);
        float[] blockRect = BlockVisualLayout.rectPx(visual, 4, 5);

        assertFalse(OcclusionFade.playerBehindOccluder(
                bounds, 56, 115, 32, 64, player, tree, blockRect));
    }

    @Test
    void playerBehindOccluder_trueWhenOpaquePixelsOverlapAndNorthOfBase() {
        SpriteAlphaMask player = solidRect(32, 64);
        SpriteAlphaMask tree = solidRect(48, 64);
        var visual = BlockVisualRegistry.get(BlockId.OAK_TREE);
        EntityBounds bounds = EntityBounds.fromFeet(PLAYER, 4.5f, 7.2f, 32, 64);
        float[] blockRect = BlockVisualLayout.rectPx(visual, 4, 5);

        assertTrue(OcclusionFade.playerBehindOccluder(
                bounds, 56, 115, 32, 64, player, tree, blockRect));
    }

    @Test
    void bush_canFadeWhenPlayerBehind() {
        assertTrue(BlockDefinitions.fadeWhenPlayerBehind(BlockId.BUSH));
        assertTrue(BlockDefinitions.triggersOcclusionFade(BlockId.BUSH));
    }

    @Test
    void stump_fadesWhenSameCellMarkedFromTree() {
        assertTrue(BlockDefinitions.fadeWhenPlayerBehind(BlockId.OAK_STUMP));
    }

    @Test
    void globalToggleDisabled_neverFades() {
        World world = TestWorlds.smallClear(12, 12);
        world.setObject(4, 5, BlockId.OAK_TREE);
        BlockWorldDrawable tree = new BlockWorldDrawable(BlockId.OAK_TREE, 4, 5);
        EntityBounds player = EntityBounds.fromFeet(PLAYER, 4.5f, 7.2f, 32, 64);

        DrawContext ctx =
                DrawContext.create(world, List.of(tree), player, 4.5f, 7.2f, null, null, false);

        assertEquals(1f, ctx.fadePlan().blockDrawAlpha(BlockId.OAK_TREE, 4, 5));
    }

    private static SpriteAlphaMask solidRect(int w, int h) {
        boolean[] opaque = new boolean[w * h];
        java.util.Arrays.fill(opaque, true);
        return SpriteAlphaMask.of(w, h, opaque);
    }

    private static boolean[] opaqueColumn(int w, int h, int columnX) {
        boolean[] opaque = new boolean[w * h];
        for (int y = 0; y < h; y++) {
            opaque[y * w + columnX] = true;
        }
        return opaque;
    }
}
