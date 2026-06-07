package com.dawn.gameplay.drops;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import java.util.List;

public final class DropRenderer {
    private static final float ICON_SIZE_PX = Constants.CELL_SIZE_PX * 0.75f;
    private static final float ICON_HALF_SIZE_CELLS = ICON_SIZE_PX / Constants.CELL_SIZE_PX / 2f;

    public static float iconHalfSizeCells() {
        return ICON_HALF_SIZE_CELLS;
    }

    public void renderSprites(SpriteBatch batch, DawnAssets assets, List<WorldDrop> drops) {
        batch.setColor(Color.WHITE);
        for (WorldDrop drop : drops) {
            drawOne(batch, assets, drop);
        }
        batch.setColor(Color.WHITE);
    }

    public static void drawOne(SpriteBatch batch, DawnAssets assets, WorldDrop drop) {
        if (drop.stack.isEmpty()) {
            return;
        }
        ItemDef def = ItemRegistry.get(drop.stack);
        if (def == null) {
            return;
        }
        TextureRegion icon = assets.item(def.iconId());
        if (icon == null) {
            return;
        }
        float cellSize = Constants.CELL_SIZE_PX;
        float px = drop.x * cellSize - ICON_SIZE_PX / 2f;
        float py = drop.y * cellSize - ICON_SIZE_PX / 2f;
        batch.draw(icon, px, py, ICON_SIZE_PX, ICON_SIZE_PX);
    }
}
