package com.dawn.world.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.gameplay.drops.WorldDrop;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import java.util.ArrayList;
import java.util.List;

public final class WorldDrawCollector {
    private WorldDrawCollector() {}

    public static List<WorldDrawable> collect(
            World world,
            int minX,
            int maxX,
            int minY,
            int maxY,
            float playerFeetX,
            float playerFeetY,
            TextureRegion playerSprite,
            List<WorldDrop> drops) {
        int cellCount = Math.max(0, maxX - minX + 1) * Math.max(0, maxY - minY + 1);
        List<WorldDrawable> out = new ArrayList<>(cellCount + 1 + drops.size());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (!world.inBounds(x, y)) {
                    continue;
                }
                BlockId blockId = world.getObject(x, y);
                if (blockId == BlockId.AIR) {
                    continue;
                }
                out.add(new BlockWorldDrawable(blockId, x, y));
            }
        }

        if (playerSprite != null) {
            out.add(new EntityWorldDrawable(playerFeetX, playerFeetY, playerSprite));
        }

        for (WorldDrop drop : drops) {
            if (drop.stack.isEmpty()) {
                continue;
            }
            out.add(new DropWorldDrawable(drop));
        }

        return out;
    }
}
