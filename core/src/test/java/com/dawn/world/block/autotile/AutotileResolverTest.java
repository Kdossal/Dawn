package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.test.TestWorlds;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutotileResolverTest {
    private AutotileFamily grass;
    private World world;

    @BeforeEach
    void setUp() {
        grass = AutotileDefinitionsLoader.load().get(BlockId.GRASS);
        world = TestWorlds.smallWalkable(5, 5);
    }

    private void setGrass(int x, int y) {
        world.setFloor(x, y, BlockId.GRASS);
    }

    @Test
    void nEdge_southEastWestNeighbors_usesTopCenterCell() {
        setGrass(2, 2);
        setGrass(1, 2);
        setGrass(3, 2);
        setGrass(2, 1);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, grass, 0);
        assertEquals(7, CardinalMask.compute(world, 2, 2, grass.neighborLayer(), grass.neighborBlockId()));
        assertEquals(1, cell.col());
        assertEquals(0, cell.row());
    }

    @Test
    void eEdge_northSouthWestNeighbors_usesEastMiddleCell() {
        setGrass(2, 2);
        setGrass(2, 3);
        setGrass(2, 1);
        setGrass(1, 2);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, grass, 0);
        assertEquals(11, CardinalMask.compute(world, 2, 2, grass.neighborLayer(), grass.neighborBlockId()));
        assertEquals(2, cell.col());
        assertEquals(1, cell.row());
    }

    @Test
    void topU_southNeighborOnly_usesTopUCell() {
        setGrass(2, 2);
        setGrass(2, 1);
        AutotileCell cell = AutotileResolver.resolve(world, 2, 2, grass, 0);
        assertEquals(2, CardinalMask.compute(world, 2, 2, grass.neighborLayer(), grass.neighborBlockId()));
        assertEquals(3, cell.col());
        assertEquals(0, cell.row());
    }
}
