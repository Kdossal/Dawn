package com.dawn.world.block.autotile;

import java.util.Arrays;
import java.util.Comparator;

/** Picks the most specific matching neighbor rule (most required bits, then col, row). */
public final class NeighborRuleMatcher {
    private static final AutotileCell ISOLATED_FALLBACK = new AutotileCell(3, 3);

    private NeighborRuleMatcher() {}

    public static NeighborRule[] sortForMatch(NeighborRule[] rules) {
        NeighborRule[] sorted = Arrays.copyOf(rules, rules.length);
        Arrays.sort(
                sorted,
                Comparator.comparingInt((NeighborRule r) -> Integer.bitCount(r.requiredMask()))
                        .reversed()
                        .thenComparingInt(NeighborRule::col)
                        .thenComparingInt(NeighborRule::row));
        return sorted;
    }

    public static AutotileCell match(int presentMask, NeighborRule[] sortedRules) {
        for (NeighborRule rule : sortedRules) {
            if ((rule.requiredMask() & presentMask) == rule.requiredMask()) {
                return rule.cell();
            }
        }
        return ISOLATED_FALLBACK;
    }
}
