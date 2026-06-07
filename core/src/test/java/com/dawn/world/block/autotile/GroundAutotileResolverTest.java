package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroundAutotileResolverTest {
    private AutotileFamily pitFamily;
    private AutotileFamily waterFamily;
    private World world;

    @BeforeEach
    void setUp() {
        var families = AutotileDefinitionsLoader.load();
        pitFamily = families.get(BlockId.PIT);
        waterFamily = families.get(BlockId.WATER);
        world = TestWorlds.smallWalkable(5, 5);
    }

    @Test
    void load_pitAndWaterFamiliesPresent() {
        assertNotNull(pitFamily);
        assertNotNull(waterFamily);
        assertEquals("PIT_GROUND", pitFamily.id());
        assertEquals("WATER_GROUND", waterFamily.id());
        assertEquals(4, pitFamily.rows());
        assertEquals(4, waterFamily.rows());
        assertFalse(pitFamily.hasCenterVariants());
        assertFalse(waterFamily.hasCenterVariants());
    }

    @Test
    void pit_isolatedUsesIsolatedTile() {
        world.setGround(2, 2, BlockId.PIT);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, pitFamily, 0);
        assertEquals(3, cell.col());
        assertEquals(0, cell.row());
    }

    @Test
    void water_cornerSe_mask6_resolvesToSeCorner() {
        world.setGround(2, 2, BlockId.WATER);
        world.setGround(3, 2, BlockId.WATER);
        world.setGround(2, 1, BlockId.WATER);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, waterFamily, 0);
        assertEquals(6, CardinalMask.compute(world, 2, 2, waterFamily.neighborLayer(), waterFamily.neighborBlockId()));
        assertEquals(1, cell.col());
        assertEquals(2, cell.row());
    }

    @Test
    void pit_fullSurround_usesSingleFullTile() {
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                world.setGround(x, y, BlockId.PIT);
            }
        }
        AutotileCell first = AutotileResolver.resolve(world, 2, 2, pitFamily, 0);
        AutotileCell second = AutotileResolver.resolve(world, 2, 2, pitFamily, 99);
        assertEquals(0, first.col());
        assertEquals(0, first.row());
        assertEquals(first, second);
    }
}
