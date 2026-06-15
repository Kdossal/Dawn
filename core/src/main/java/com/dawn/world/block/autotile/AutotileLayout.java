package com.dawn.world.block.autotile;

/** How autotile sheet cells map to neighbor patterns. */
public enum AutotileLayout {
    /** Explicit mask → col/row entries in autotiles.json. */
    CUSTOM,
    /** Fixed 4×4 blob grid; mask lookup is derived at load time. */
    STANDARD_BLOB,
    /** 8-neighbor rule list; most specific matching rule wins. */
    NEIGHBOR_RULES
}
