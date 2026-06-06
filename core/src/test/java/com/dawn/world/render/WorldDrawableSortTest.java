package com.dawn.world.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.gameplay.drops.WorldDrop;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.visual.BlockVisualLayout;
import com.dawn.world.block.visual.BlockVisualRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorldDrawableSortTest {

    @Test
    void higherSortY_drawnFirst() {
        List<WorldDrawable> list = new ArrayList<>();
        list.add(block(BlockId.ROCK, 0, 5));
        list.add(block(BlockId.ROCK, 0, 10));
        WorldDrawableSort.sort(list);
        assertTrue(list.get(0).sortY() > list.get(1).sortY());
    }

    @Test
    void sameY_lowerSortX_drawnFirst() {
        List<WorldDrawable> list = new ArrayList<>();
        list.add(block(BlockId.ROCK, 8, 10));
        list.add(block(BlockId.ROCK, 3, 10));
        WorldDrawableSort.sort(list);
        assertTrue(list.get(0).sortX() < list.get(1).sortX());
    }

    @Test
    void cellTile_sortKeyIsCellBottomRight() {
        BlockWorldDrawable drawable = block(BlockId.ROCK, 4, 7);
        assertEquals(7f, drawable.sortY(), 1e-5f);
        assertEquals(5f, drawable.sortX(), 1e-5f);
    }

    @Test
    void tree_sortKeyUsesSpriteBottomRight_notOccupancyCorner() {
        float[] key = BlockVisualLayout.bottomRightCell(BlockVisualRegistry.get(BlockId.OAK_TREE), 5, 7);
        BlockWorldDrawable tree = block(BlockId.OAK_TREE, 5, 7);
        assertEquals(key[0], tree.sortY(), 1e-5f);
        assertEquals(key[1], tree.sortX(), 1e-5f);
        assertTrue(tree.sortX() > 6f);
        assertEquals(7f, tree.sortY(), 1e-5f);
    }

    @Test
    void playerSouthOfBush_drawsOnTop() {
        BlockWorldDrawable bush = block(BlockId.BUSH, 4, 7);
        EntityWorldDrawable player = new EntityWorldDrawable(4.5f, 6.5f, 32);
        List<WorldDrawable> list = new ArrayList<>(List.of(bush, player));
        WorldDrawableSort.sort(list);
        assertEquals(player, list.get(1));
    }

    @Test
    void playerNorthOfBush_drawsBehind() {
        BlockWorldDrawable bush = block(BlockId.BUSH, 4, 7);
        EntityWorldDrawable player = new EntityWorldDrawable(4.5f, 7.5f, 32);
        List<WorldDrawable> list = new ArrayList<>(List.of(bush, player));
        WorldDrawableSort.sort(list);
        assertEquals(player, list.get(0));
    }

    @Test
    void sameSortKey_depthBiasOrdersBlockEntityDrop() {
        float halfIcon = com.dawn.gameplay.drops.DropRenderer.iconHalfSizeCells();
        float sortY = 6f;
        float sortX = 4f;
        BlockWorldDrawable bush = block(BlockId.BUSH, 3, 6);
        EntityWorldDrawable player = new EntityWorldDrawable(sortX, sortY, null);
        WorldDrop drop = new WorldDrop(new ItemStack(ItemId.DIRT_CLUMP, 1), sortX - halfIcon, sortY + halfIcon, 0f);
        DropWorldDrawable dropDrawable = new DropWorldDrawable(drop);
        assertEquals(sortY, bush.sortY(), 1e-5f);
        assertEquals(sortX, bush.sortX(), 1e-5f);
        assertEquals(sortY, player.sortY(), 1e-5f);
        assertEquals(sortX, player.sortX(), 1e-5f);
        assertEquals(sortY, dropDrawable.sortY(), 1e-5f);
        assertEquals(sortX, dropDrawable.sortX(), 1e-5f);

        List<WorldDrawable> list = new ArrayList<>(List.of(dropDrawable, player, bush));
        WorldDrawableSort.sort(list);
        assertEquals(WorldDepthBias.block(BlockId.BUSH), list.get(0).depthBias());
        assertEquals(WorldDepthBias.ENTITY, list.get(1).depthBias());
        assertEquals(WorldDepthBias.DROP, list.get(2).depthBias());
    }

    private static BlockWorldDrawable block(BlockId id, int x, int y) {
        return new BlockWorldDrawable(id, x, y);
    }
}
