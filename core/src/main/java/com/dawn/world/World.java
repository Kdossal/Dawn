package com.dawn.world;

import com.dawn.config.Constants;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import com.dawn.world.block.SurfaceRules;
import com.dawn.world.structure.StructureRegistry;

public class World {
    private final int width;
    private final int height;
    private final BlockId[][] ground;
    private final BlockId[][] floor;
    private final BlockId[][] objects;
    private final StructureRegistry structures = new StructureRegistry();
    private final BlockDamageStore blockDamage = new BlockDamageStore();

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.ground = new BlockId[width][height];
        this.floor = new BlockId[width][height];
        this.objects = new BlockId[width][height];
        WorldMaps.fillPlayground(this);
    }

    public static World createDefault() {
        return new World(Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public StructureRegistry getStructures() {
        return structures;
    }

    public BlockDamageStore getBlockDamage() {
        return blockDamage;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public BlockId getGround(int x, int y) {
        return ground[x][y];
    }

    public BlockId getFloor(int x, int y) {
        return floor[x][y];
    }

    public BlockId getObject(int x, int y) {
        return objects[x][y];
    }

    public void setGround(int x, int y, BlockId id) {
        if (inBounds(x, y)) {
            ground[x][y] = id;
            if (id == BlockId.PIT) {
                blockDamage.clear(Layer.GROUND, x, y);
            }
        }
    }

    public void setFloor(int x, int y, BlockId id) {
        if (inBounds(x, y)) {
            floor[x][y] = id;
            if (id == BlockId.AIR) {
                blockDamage.clear(Layer.FLOOR, x, y);
            }
        }
    }

    public void setObject(int x, int y, BlockId id) {
        if (inBounds(x, y)) {
            objects[x][y] = id;
            if (id == BlockId.AIR) {
                blockDamage.clear(Layer.OBJECT, x, y);
            }
        }
    }

    public boolean isSolidForMovement(int cellX, int cellY) {
        return !SurfaceRules.canWalk(this, cellX, cellY);
    }

    public Layer getPrimaryInteractLayer(int x, int y) {
        if (!inBounds(x, y)) {
            return null;
        }
        BlockId object = objects[x][y];
        if (object != BlockId.AIR && isBreakable(object)) {
            return Layer.OBJECT;
        }
        BlockId floorId = floor[x][y];
        if (floorId != BlockId.AIR && isBreakable(floorId)) {
            return Layer.FLOOR;
        }
        BlockId groundId = ground[x][y];
        if (groundId != BlockId.PIT && isBreakable(groundId)) {
            return Layer.GROUND;
        }
        return null;
    }

    private static boolean isBreakable(BlockId id) {
        BlockDefinitions.BlockDef def = BlockDefinitions.get(id);
        return def != null && def.breakable();
    }

    public BlockId getBlockAtLayer(int x, int y, Layer layer) {
        return switch (layer) {
            case GROUND -> getGround(x, y);
            case FLOOR -> getFloor(x, y);
            case OBJECT -> getObject(x, y);
        };
    }

    public void setBlockAtLayer(int x, int y, Layer layer, BlockId id) {
        switch (layer) {
            case GROUND -> setGround(x, y, id);
            case FLOOR -> setFloor(x, y, id);
            case OBJECT -> setObject(x, y, id);
        }
    }
}
