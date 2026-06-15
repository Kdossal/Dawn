package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoneWallAutotileResolverTest {
    private AutotileFamily family;
    private World world;

    @BeforeEach
    void setUp() {
        family = AutotileDefinitionsLoader.load().get(BlockId.STONE_WALL);
        world = TestWorlds.smallWalkable(5, 5);
    }

    private void setWall(int x, int y) {
        world.setObject(x, y, BlockId.STONE_WALL);
    }

    @Test
    void isolatedWall_usesIsolatedTile() {
        setWall(2, 2);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, family, 0);
        assertEquals(3, cell.col());
        assertEquals(3, cell.row());
        assertEquals(0, CardinalMask.compute(world, 2, 2, family.neighborLayer(), family.neighborBlockId()));
    }

    @Test
    void cornerSe_mask6_resolvesToSeCorner() {
        setWall(2, 2);
        setWall(3, 2);
        setWall(2, 1);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, family, 0);
        assertEquals(6, CardinalMask.compute(world, 2, 2, family.neighborLayer(), family.neighborBlockId()));
        assertEquals(0, cell.col());
        assertEquals(0, cell.row());
    }

    @Test
    void fullSurround_usesCenterTile() {
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                setWall(x, y);
            }
        }
        int mask = CardinalMask.compute(world, 2, 2, family.neighborLayer(), family.neighborBlockId());
        assertEquals(AutotileFamily.FULL_SURROUND_MASK, mask);

        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, family, 42);
        assertEquals(1, cell.col());
        assertEquals(1, cell.row());
    }
}
