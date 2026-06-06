package com.dawn.world.block.visual;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BlockVisualDefinitionsLoaderTest {

    @Test
    void parseJson_duplicateBlockIdFails() {
        String dup =
                """
                {"entries":[
                  {"blockId":"STONE","textureId":"STONE","width":16,"height":16,"anchor":"CELL_BOTTOM_LEFT"},
                  {"blockId":"STONE","textureId":"STONE","width":16,"height":16,"anchor":"CELL_BOTTOM_LEFT"}
                ]}""";
        assertThrows(IllegalArgumentException.class, () -> BlockVisualDefinitionsLoader.parseJson(
                new java.io.ByteArrayInputStream(dup.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }

    @Test
    void parseJson_singleEntryParsesOffsetsAndAlphaDefaults() {
        String json =
                """
                {"entries":[
                  {"blockId":"CRATE","textureId":"CRATE","width":20,"height":18,"anchor":"CELL_BOTTOM_CENTER","offsetX":1,"offsetY":2,"alpha":0.9}
                ]}""";
        var map = BlockVisualDefinitionsLoader.parseJson(
                new java.io.ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        BlockVisualDef d = map.get(com.dawn.world.block.BlockId.CRATE);
        assertNotNull(d);
        assertEquals(20, d.widthPx());
        assertEquals(18, d.heightPx());
        assertEquals(VisualAnchor.CELL_BOTTOM_CENTER, d.anchor());
        assertEquals(1, d.offsetPxX());
        assertEquals(2, d.offsetPxY());
        assertEquals(0.9f, d.defaultAlpha(), 1e-5f);
    }
}
