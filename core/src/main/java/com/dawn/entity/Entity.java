package com.dawn.entity;

import com.dawn.assets.DawnAssets;
import com.dawn.world.World;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Runtime creature instance. {@link #getX()} / {@link #getY()} are feet bottom-center in cell space. */
public final class Entity {
    private final EntityId entityId;
    private final EntityDef def;
    private final Stats stats;
    private float x;
    private float y;
    private float currentHp;
    private float maxHp;
    private boolean lastMoveApplied;

    public Entity(EntityId entityId, float startX, float startY) {
        EntityDef entityDef = EntityRegistry.get(entityId);
        if (entityDef == null) {
            throw new IllegalArgumentException("Unknown entity: " + entityId);
        }
        this.entityId = entityId;
        this.def = entityDef;
        this.stats = new Stats(entityDef.defaultStats());
        this.x = startX;
        this.y = startY;
        refreshVitals();
    }

    public EntityId getEntityId() {
        return entityId;
    }

    public EntityDef def() {
        return def;
    }

    public Stats getStats() {
        return stats;
    }

    /** Feet bottom-center X in world cells. */
    public float getX() {
        return x;
    }

    /** Feet bottom-center Y in world cells. */
    public float getY() {
        return y;
    }

    public float getCurrentHp() {
        return currentHp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getArmor() {
        return StatFormulas.armor(stats);
    }

    public float getMoveSpeedCellsPerSec() {
        return getMoveSpeedCellsPerSec(false);
    }

    public float getMoveSpeedCellsPerSec(boolean running) {
        return StatFormulas.moveSpeedCellsPerSec(stats, running);
    }

    public boolean wasLastMoveApplied() {
        return lastMoveApplied;
    }

    public TextureRegion resolveSprite(DawnAssets assets) {
        return assets.textureForEntity(def);
    }

    public EntityBounds bounds(DawnAssets assets) {
        TextureRegion sprite = resolveSprite(assets);
        int w = sprite == null ? 0 : sprite.getRegionWidth();
        int h = sprite == null ? 0 : sprite.getRegionHeight();
        return EntityBounds.fromFeet(def, x, y, w, h);
    }

    public void refreshVitals() {
        maxHp = StatFormulas.maxHealth(stats);
        if (currentHp <= 0f || currentHp > maxHp) {
            currentHp = maxHp;
        }
    }

    /** True if the movement footprint overlaps this cell. */
    public boolean occupiesCell(int cellX, int cellY) {
        return EntityCollision.overlapsCell(boundsForMovement(), cellX, cellY);
    }

    public void move(float dx, float dy, World world) {
        EntityMovementSolver.Result result = EntityMovementSolver.move(def, x, y, dx, dy, world);
        x = result.feetX();
        y = result.feetY();
        lastMoveApplied = result.moved();
    }

    private EntityBounds boundsForMovement() {
        return EntityBounds.fromFeet(def, x, y, 0, 0);
    }
}
