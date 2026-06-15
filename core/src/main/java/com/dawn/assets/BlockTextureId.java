package com.dawn.assets;

/** Texture id = PNG stem under {@code assets/tiles/} (see {@link #fileName}). Authoring: {@code block_visuals.json}. */
public enum BlockTextureId {
    PIT("pit"),
    GRASS("grass"),
    DIRT("dirt"),
    SAND("sand"),
    STONE("stone"),
    WATER("water"),
    ROCK("rock"),
    BUSH("bush"),
    OAK_TREE("tree"),
    OAK_STUMP("tree_stump"),
    SPRUCE_TREE("spruce_tree"),
    SPRUCE_STUMP("spruce_stump"),
    CRATE("crate"),
    BED_FOOT("bed_foot"),
    BED_HEAD("bed_head"),
    LANTERN("lantern"),
    STONE_WALL("stone_wall");

    public final String fileName;

    BlockTextureId(String fileName) {
        this.fileName = fileName;
    }

    /** Tall sprites that need precomputed alpha masks for occlusion fade. */
    public boolean needsOcclusionMask() {
        return this == BUSH || this == OAK_TREE || this == SPRUCE_TREE;
    }
}
