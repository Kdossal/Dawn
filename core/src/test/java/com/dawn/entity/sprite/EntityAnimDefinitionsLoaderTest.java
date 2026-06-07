package com.dawn.entity.sprite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EntityAnimDefinitionsLoaderTest {

    @Test
    void parseJson_duplicateRowFails() {
        String json =
                """
                {"entities":[{
                  "entityId":"PLAYER","spriteId":"player","frameWidth":16,"frameHeight":25,"cols":4,"rows":7,
                  "clips":{
                    "idle":{"row":0,"frameCount":4,"fps":4},
                    "walk_down":{"row":0,"frameCount":4,"fps":8}
                  }
                }]}""";
        assertThrows(
                IllegalArgumentException.class,
                () -> EntityAnimDefinitionsLoader.parseJson(
                        new java.io.ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }
}
