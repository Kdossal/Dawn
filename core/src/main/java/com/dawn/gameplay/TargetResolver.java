package com.dawn.gameplay;

import com.dawn.entity.EntityDef;
import com.dawn.item.ItemStack;
import com.dawn.world.World;

public final class TargetResolver {
    public record TargetCell(int x, int y, boolean clamped) {}

    private TargetResolver() {}

    public static TargetCell resolve(
            World world,
            EntityDef playerDef,
            float feetX,
            float feetY,
            int mouseCellX,
            int mouseCellY,
            ItemStack held) {
        if (!world.inBounds(mouseCellX, mouseCellY)) {
            return null;
        }

        int radius = ReachResolver.radiusCellsForHeld(held);
        float[] center = ReachResolver.reachCenter(playerDef, feetX, feetY);

        float targetCx = mouseCellX + 0.5f;
        float targetCy = mouseCellY + 0.5f;
        float dx = targetCx - center[0];
        float dy = targetCy - center[1];
        float dist = (float) Math.hypot(dx, dy);
        if (dist > radius) {
            return null;
        }

        if (!ReachResolver.inReach(playerDef, feetX, feetY, mouseCellX, mouseCellY, radius)) {
            return null;
        }
        return new TargetCell(mouseCellX, mouseCellY, false);
    }
}
