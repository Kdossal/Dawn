package com.dawn.world.block.autotile;

import static org.junit.jupiter.api.Assertions.*;

import com.dawn.world.block.BlockId;
import org.junit.jupiter.api.Test;

class AutotileDefinitionsLoaderTest {

    @Test
    void load_allFamiliesPresent() {
        var map = AutotileDefinitionsLoader.load();
        AutotileFamily grass = map.get(BlockId.GRASS);
        AutotileFamily pit = map.get(BlockId.PIT);
        AutotileFamily water = map.get(BlockId.WATER);
        assertNotNull(grass);
        assertNotNull(pit);
        assertNotNull(water);
        assertEquals("GRASS_FLOOR", grass.id());
        assertEquals(4, grass.cols());
        assertEquals(5, grass.rows());
        assertEquals(4, grass.centerTiles().length);
        assertFalse(grass.hasNeighborRules());
        assertEquals(11, pit.cols());
        assertEquals(4, pit.rows());
        assertTrue(pit.hasNeighborRules());
        assertEquals(43, pit.neighborRules().length);
        assertEquals(11, water.cols());
        assertTrue(water.hasNeighborRules());
        assertEquals(new AutotileCell(3, 3), grass.tileForMask(0));

        AutotileFamily wall = map.get(BlockId.STONE_WALL);
        assertNotNull(wall);
        assertEquals("STONE_WALL_OBJECT", wall.id());
        assertEquals(16, wall.tileWidthPx());
        assertEquals(32, wall.tileHeightPx());
        assertEquals(0, wall.centerTiles().length);
    }

    @Test
    void parseJson_rectangularTileHeight() {
        String json =
                """
                {"families":[{
                  "id":"TEST","layout":"STANDARD_BLOB","blockId":"STONE_WALL","textureId":"STONE_WALL",
                  "cols":4,"rows":4,"tileSizePx":16,"tileHeightPx":32,
                  "neighborLayer":"OBJECT","neighborBlockId":"STONE_WALL"
                }]}""";
        var map =
                AutotileDefinitionsLoader.parseJson(
                        new java.io.ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        AutotileFamily family = map.get(BlockId.STONE_WALL);
        assertEquals(16, family.tileWidthPx());
        assertEquals(32, family.tileHeightPx());
    }

    @Test
    void parseJson_standardBlobWithCenterRow() {
        String json =
                """
                {"families":[{
                  "id":"TEST","layout":"STANDARD_BLOB","blockId":"GRASS","textureId":"GRASS",
                  "cols":4,"rows":5,"tileSizePx":16,
                  "neighborLayer":"FLOOR","neighborBlockId":"GRASS",
                  "centerRow":4
                }]}""";
        var map =
                AutotileDefinitionsLoader.parseJson(
                        new java.io.ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        AutotileFamily family = map.get(BlockId.GRASS);
        assertEquals(4, family.centerTiles().length);
        assertEquals(new AutotileCell(0, 0), family.tileForMask(6));
    }

    @Test
    void parseJson_neighborRulesLayout() {
        String json =
                """
                {"families":[{
                  "id":"TEST","layout":"NEIGHBOR_RULES","blockId":"PIT","textureId":"PIT",
                  "cols":11,"rows":4,"tileSizePx":16,
                  "neighborLayer":"GROUND","neighborBlockId":"PIT",
                  "neighborRules":[
                    {"col":3,"row":3,"neighbors":[]},
                    {"col":1,"row":1,"neighbors":["N","E","S","W","NE","SE","SW","NW"]}
                  ]
                }]}""";
        var map =
                AutotileDefinitionsLoader.parseJson(
                        new java.io.ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        AutotileFamily family = map.get(BlockId.PIT);
        assertTrue(family.hasNeighborRules());
        assertEquals(2, family.neighborRules().length);
        assertEquals(EightNeighborMask.ALL, family.neighborRules()[0].requiredMask());
    }

    @Test
    void parseJson_standardBlobRejectsTilesArray() {
        String json =
                """
                {"families":[{
                  "id":"TEST","layout":"STANDARD_BLOB","blockId":"PIT","textureId":"PIT",
                  "cols":4,"rows":4,"tileSizePx":16,
                  "neighborLayer":"GROUND","neighborBlockId":"PIT",
                  "tiles":[{"mask":0,"col":0,"row":0}]
                }]}""";
        assertThrows(
                IllegalArgumentException.class,
                () -> AutotileDefinitionsLoader.parseJson(
                        new java.io.ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
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
