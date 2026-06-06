package com.dawn.world.block.visual;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import org.junit.jupiter.api.Test;

class BlockVisualRegistrationTest {

    /** Every authored block tile (GROUND, FLOOR, OBJECT) must have an entry in {@link BlockVisualRegistry}. */
    @Test
    void allGameplayBlocksHaveSprites() {
        for (BlockId id : BlockId.values()) {
            if (id == BlockId.AIR) {
                continue;
            }
            BlockDefinitions.BlockDef d = BlockDefinitions.get(id);
            if (d == null) {
                continue;
            }
            Layer layer = d.layer();
            if (layer != Layer.GROUND && layer != Layer.FLOOR && layer != Layer.OBJECT) {
                continue;
            }
            assertTrue(BlockVisualRegistry.hasVisual(id), "missing block_visuals.json entry for " + id);
            assertNotNull(BlockVisualRegistry.get(id));
        }
    }
}
