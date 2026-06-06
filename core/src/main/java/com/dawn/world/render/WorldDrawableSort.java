package com.dawn.world.render;

import java.util.Comparator;
import java.util.List;

/** Y-sort by bottom-right footprint: higher Y first, then lower X, then depth bias. */
public final class WorldDrawableSort {
    private static final Comparator<WorldDrawable> COMPARATOR = Comparator.comparing(WorldDrawable::sortY)
            .reversed()
            .thenComparing(WorldDrawable::sortX)
            .thenComparingInt(WorldDrawable::depthBias);

    private WorldDrawableSort() {}

    public static void sort(List<WorldDrawable> drawables) {
        drawables.sort(COMPARATOR);
    }
}
