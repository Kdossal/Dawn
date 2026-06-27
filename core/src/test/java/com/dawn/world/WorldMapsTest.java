package com.dawn.world;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dawn.config.Constants;
import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class WorldMapsTest {
    @Test
    void playgroundPlacesCratesNearSpawn() {
        World world = World.createDefault();
        int cx = Constants.MAP_WIDTH / 2;
        int cy = Constants.MAP_HEIGHT / 2;
        int count = 0;
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -5; dy <= 5; dy++) {
                int x = cx + dx;
                int y = cy + dy;
                if (world.getObject(x, y) == BlockId.CRATE) {
                    count++;
                    assertTrue(world.getCrateStorage().hasAt(x, y));
                }
            }
        }
        assertTrue(count >= 2, "expected at least 2 starter crates near spawn, found " + count);
    }
}
