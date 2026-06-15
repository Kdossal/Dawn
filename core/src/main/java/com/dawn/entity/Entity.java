package com.dawn.entity;

import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.config.GameConfig;
import com.dawn.entity.sprite.EntityAnimClip;
import com.dawn.entity.sprite.EntityAnimDef;
import com.dawn.entity.sprite.EntityAnimRegistry;
import com.dawn.entity.sprite.EntityAnimResolver;
import com.dawn.entity.sprite.EntitySpriteFrame;
import com.dawn.entity.sprite.Facing4;
import com.dawn.entity.sprite.PlayerAnimContext;
import com.dawn.entity.status.StatusModifiers;
import com.dawn.entity.status.StatusSet;
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
    private float currentEnergy;
    private float maxEnergy;
    private float currentHunger;
    private float maxHunger;
    private float currentThirst;
    private float maxThirst;
    private boolean vitalsInitialized;
    private boolean lastMoveApplied;
    private Facing4 facing = Facing4.DOWN;
    private String currentClipId = "idle_down";
    private float animStateTime;
    private final StatusSet statuses = new StatusSet();
    private float poisonTimeRemaining;

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

    public float getCurrentEnergy() {
        return currentEnergy;
    }

    public float getMaxEnergy() {
        return maxEnergy;
    }

    public float getCurrentHunger() {
        return currentHunger;
    }

    public float getMaxHunger() {
        return maxHunger;
    }

    public float getCurrentThirst() {
        return currentThirst;
    }

    public float getMaxThirst() {
        return maxThirst;
    }

    public float getArmor() {
        return StatFormulas.armor(stats);
    }

    public float getMoveSpeedCellsPerSec() {
        return getMoveSpeedCellsPerSec(false);
    }

    public float getMoveSpeedCellsPerSec(boolean running) {
        float base = StatFormulas.moveSpeedCellsPerSec(stats, running);
        return base * StatusModifiers.moveSpeedMultiplier(statuses);
    }

    public StatusSet getStatuses() {
        return statuses;
    }

    public boolean isPoisoned() {
        return poisonTimeRemaining > 0f;
    }

    public float getPoisonTimeRemaining() {
        return poisonTimeRemaining;
    }

    public void setPoisoned(boolean poisoned) {
        poisonTimeRemaining = poisoned ? GameConfig.get().poisonDurationSec : 0f;
    }

    public void tickEffects(float delta) {
        if (delta <= 0f || poisonTimeRemaining <= 0f) {
            return;
        }
        poisonTimeRemaining = Math.max(0f, poisonTimeRemaining - delta);
    }

    public void setHunger(float hunger) {
        currentHunger = Math.max(0f, hunger);
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
        GameConfig cfg = GameConfig.get();
        maxHp = StatFormulas.maxHealth(stats);
        maxEnergy = StatFormulas.maxEnergy(stats);
        maxHunger = cfg.maxHunger;
        maxThirst = cfg.maxThirst;

        if (!vitalsInitialized) {
            currentHp = maxHp;
            currentEnergy = maxEnergy;
            if (entityId == EntityId.PLAYER) {
                currentHunger = 10f;
                poisonTimeRemaining = cfg.poisonDurationSec;
            } else {
                currentHunger = maxHunger;
            }
            currentThirst = maxThirst;
            vitalsInitialized = true;
            return;
        }

        if (currentHp <= 0f || currentHp > maxHp) {
            currentHp = maxHp;
        }
        if (currentEnergy <= 0f || currentEnergy > maxEnergy) {
            currentEnergy = maxEnergy;
        }
        if (currentHunger < 0f) {
            currentHunger = 0f;
        }
        if (currentThirst > maxThirst) {
            currentThirst = maxThirst;
        }
    }

    /** Applies deltas to vital pools (positive regen, negative drain). */
    public void adjustVitals(float hpDelta, float energyDelta, float hungerDelta, float thirstDelta) {
        currentHp = clamp(currentHp + hpDelta, 0f, maxHp);
        currentEnergy = clamp(currentEnergy + energyDelta, 0f, maxEnergy);
        currentHunger = Math.max(0f, currentHunger + hungerDelta);
        currentThirst = clamp(currentThirst + thirstDelta, 0f, maxThirst);
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

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
