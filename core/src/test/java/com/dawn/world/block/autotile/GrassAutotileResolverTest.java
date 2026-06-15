package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrassAutotileResolverTest {
    private AutotileFamily family;
    private World world;

    @BeforeEach
    void setUp() {
        family = AutotileDefinitionsLoader.load().get(BlockId.GRASS);
        world = TestWorlds.smallWalkable(5, 5);
    }

    private void setGrass(int x, int y) {
        world.setFloor(x, y, BlockId.GRASS);
    }

    @Test
    void isolatedGrass_usesIsolatedTile() {
        setGrass(2, 2);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, family, 0);
        assertEquals(3, cell.col());
        assertEquals(3, cell.row());
        assertEquals(0, CardinalMask.compute(world, 2, 2, family.neighborLayer(), family.neighborBlockId()));
    }

    @Test
    void cornerSe_mask6_resolvesToSeCorner() {
        setGrass(2, 2);
        setGrass(3, 2);
        setGrass(2, 1);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, family, 0);
        assertEquals(6, CardinalMask.compute(world, 2, 2, family.neighborLayer(), family.neighborBlockId()));
        assertEquals(0, cell.col());
        assertEquals(0, cell.row());
    }

    @Test
    void fullSurround_usesCenterVariant() {
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                setGrass(x, y);
            }
        }
        int mask = CardinalMask.compute(world, 2, 2, family.neighborLayer(), family.neighborBlockId());
        assertEquals(AutotileFamily.FULL_SURROUND_MASK, mask);

        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, family, 42);
        AutotileCell expected = family.tileForFullSurround(2, 2, 42);
        assertEquals(expected.col(), cell.col());
        assertEquals(expected.row(), cell.row());
    }

    @Test
    void centerVariant_stableForSameCellAndSeed() {
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                setGrass(x, y);
            }
        }
        AutotileCell first = AutotileResolver.resolve(world, 2, 2, family, 99);
        AutotileCell second = AutotileResolver.resolve(world, 2, 2, family, 99);
        assertEquals(first, second);
    }

    @Test
    void centerVariant_canDifferAcrossCells() {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                setGrass(x, y);
            }
        }
        boolean sawDifference = false;
        AutotileCell baseline = AutotileResolver.resolve(world, 0, 0, family, 7);
        for (int x = 0; x < 5 && !sawDifference; x++) {
            for (int y = 0; y < 5; y++) {
                AutotileCell cell = AutotileResolver.resolve(world, x, y, family, 7);
                if (!cell.equals(baseline)) {
                    sawDifference = true;
                    break;
                }
            }
        }
        assertTrue(sawDifference, "expected at least one differing center variant across cells");
    }
}
