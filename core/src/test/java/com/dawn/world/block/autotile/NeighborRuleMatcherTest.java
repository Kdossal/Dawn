package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NeighborRuleMatcherTest {
    private static final NeighborRule[] RULES =
            NeighborRuleMatcher.sortForMatch(
                    new NeighborRule[] {
                        new NeighborRule(EightNeighborMask.SOUTH, 3, 0),
                        new NeighborRule(
                                EightNeighborMask.EAST
                                        | EightNeighborMask.SOUTH
                                        | EightNeighborMask.SOUTH_EAST,
                                0,
                                0),
                        new NeighborRule(0, 3, 3),
                    });

    @Test
    void match_southOnly_usesMostSpecificSouthRule() {
        AutotileCell cell = NeighborRuleMatcher.match(EightNeighborMask.SOUTH, RULES);
        assertEquals(3, cell.col());
        assertEquals(0, cell.row());
    }

    @Test
    void match_southEastAndEast_usesCornerRule() {
        int present =
                EightNeighborMask.EAST | EightNeighborMask.SOUTH | EightNeighborMask.SOUTH_EAST;
        AutotileCell cell = NeighborRuleMatcher.match(present, RULES);
        assertEquals(0, cell.col());
        assertEquals(0, cell.row());
    }

    @Test
    void match_noNeighbors_fallsBackToIsolated() {
        AutotileCell cell = NeighborRuleMatcher.match(0, RULES);
        assertEquals(3, cell.col());
        assertEquals(3, cell.row());
    }

    @Test
    void match_tieBreaksByLowerColumn() {
        NeighborRule[] rules =
                NeighborRuleMatcher.sortForMatch(
                        new NeighborRule[] {
                            new NeighborRule(EightNeighborMask.EAST, 2, 0),
                            new NeighborRule(EightNeighborMask.EAST, 0, 3),
                        });
        AutotileCell cell = NeighborRuleMatcher.match(EightNeighborMask.EAST, rules);
        assertEquals(0, cell.col());
        assertEquals(3, cell.row());
    }
}
