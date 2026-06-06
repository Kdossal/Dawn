package com.dawn.assets;

import com.dawn.render.SpriteAlphaMask;
import java.util.EnumMap;
import java.util.Map;

/** Precomputed alpha masks for pixel-accurate occlusion fade. Populated during {@link DawnAssets} load. */
public final class OcclusionMasks {
    private SpriteAlphaMask player;
    private final Map<BlockTextureId, SpriteAlphaMask> blocks = new EnumMap<>(BlockTextureId.class);

    void registerPlayer(SpriteAlphaMask mask) {
        this.player = mask;
    }

    void registerBlock(BlockTextureId id, SpriteAlphaMask mask) {
        blocks.put(id, mask);
    }

    public SpriteAlphaMask player() {
        return player;
    }

    public SpriteAlphaMask block(BlockTextureId textureId) {
        return textureId == null ? null : blocks.get(textureId);
    }
}
