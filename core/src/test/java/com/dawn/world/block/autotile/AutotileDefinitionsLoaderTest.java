package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class AutotileDefinitionsLoaderTest {

    @Test
    void load_grassFloorFamilyPresent() {
        var map = AutotileDefinitionsLoader.load();
        AutotileFamily family = map.get(BlockId.GRASS);
        assertNotNull(family);
        assertEquals("GRASS_FLOOR", family.id());
        assertEquals(4, family.cols());
        assertEquals(5, family.rows());
        assertEquals(5, family.centerTiles().length);
    }

    @Test
    void parseJson_duplicateMaskFails() {
        String json =
                """
                {"families":[{
                  "id":"TEST","blockId":"GRASS","textureId":"GRASS","cols":4,"rows":5,"tileSizePx":16,
                  "neighborLayer":"FLOOR","neighborBlockId":"GRASS",
                  "tiles":[
                    {"mask":0,"col":0,"row":0},
                    {"mask":0,"col":1,"row":0},
                    {"mask":1,"col":1,"row":1},{"mask":2,"col":2,"row":1},{"mask":3,"col":2,"row":2},
                    {"mask":4,"col":1,"row":1},{"mask":5,"col":2,"row":0},{"mask":6,"col":1,"row":2},
                    {"mask":7,"col":0,"row":3},{"mask":8,"col":0,"row":1},{"mask":9,"col":3,"row":2},
                    {"mask":10,"col":1,"row":0},{"mask":11,"col":1,"row":3},{"mask":12,"col":0,"row":2},
                    {"mask":13,"col":2,"row":3},{"mask":14,"col":3,"row":3}
                  ],
                  "centerTiles":[{"col":0,"row":0}]
                }]}""";
        assertThrows(
                IllegalArgumentException.class,
                () -> AutotileDefinitionsLoader.parseJson(
                        new java.io.ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }
}
