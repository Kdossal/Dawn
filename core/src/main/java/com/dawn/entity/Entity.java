package com.dawn.entity;

import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.entity.sprite.EntityAnimClip;
import com.dawn.entity.sprite.EntityAnimDef;
import com.dawn.entity.sprite.EntityAnimRegistry;
import com.dawn.entity.sprite.EntityAnimResolver;
import com.dawn.entity.sprite.EntitySpriteFrame;
import com.dawn.entity.sprite.Facing4;
import com.dawn.entity.sprite.PlayerAnimContext;
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
    private Facing4 facing = Facing4.DOWN;
    private String currentClipId = "idle_down";
    private float animStateTime;

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

    public void updateAnimation(float delta, PlayerAnimContext ctx) {
        if (entityId != EntityId.PLAYER || ctx == null) {
            return;
        }
        EntityAnimResolver.Selection selection = EntityAnimResolver.selectClip(ctx, facing);
        if (!selection.clipId().equals(currentClipId) || selection.facing() != facing) {
            animStateTime = 0f;
        }
        currentClipId = selection.clipId();
        facing = selection.facing();
        animStateTime += delta;
    }

    public EntitySpriteFrame resolveSpriteFrame(DawnAssets assets) {
        if (entityId != EntityId.PLAYER) {
            TextureRegion region = assets.textureForEntity(def);
            if (region == null) {
                return null;
            }
            return new EntitySpriteFrame(
                    region, false, region.getRegionWidth(), region.getRegionHeight());
        }
        EntityAnimDef animDef = EntityAnimRegistry.get(entityId);
        if (animDef == null) {
            return null;
        }
        EntityAnimClip clip = animDef.clip(currentClipId);
        int frameCol = EntityAnimResolver.frameIndex(clip, animStateTime);
        TextureRegion region = assets.entityFrame(entityId, frameCol, clip.row());
        if (region == null) {
            return null;
        }
        return new EntitySpriteFrame(
                region,
                facing.flipX(),
                animDef.frameWidth(),
                animDef.frameHeight());
    }

    public TextureRegion resolveSprite(DawnAssets assets) {
        EntitySpriteFrame frame = resolveSpriteFrame(assets);
        return frame == null ? null : frame.region();
    }

    public EntityBounds bounds(DawnAssets assets) {
        EntitySpriteFrame frame = resolveSpriteFrame(assets);
        int w = frame == null ? Constants.PLAYER_SPRITE_WIDTH_PX : frame.widthPx();
        int h = frame == null ? Constants.PLAYER_SPRITE_HEIGHT_PX : frame.heightPx();
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
