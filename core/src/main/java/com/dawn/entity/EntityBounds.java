package com.dawn.entity;

import com.dawn.config.Constants;

/** Axis-aligned boxes for an entity with feet at {@code (feetX, feetY)} in cell space. */
public final class EntityBounds {
    public final float moveLeft;
    public final float moveRight;
    public final float moveBottom;
    public final float moveTop;

    public final float spriteLeft;
    public final float spriteRight;
    public final float spriteBottom;
    public final float spriteTop;

    private EntityBounds(
            float moveLeft,
            float moveRight,
            float moveBottom,
            float moveTop,
            float spriteLeft,
            float spriteRight,
            float spriteBottom,
            float spriteTop) {
        this.moveLeft = moveLeft;
        this.moveRight = moveRight;
        this.moveBottom = moveBottom;
        this.moveTop = moveTop;
        this.spriteLeft = spriteLeft;
        this.spriteRight = spriteRight;
        this.spriteBottom = spriteBottom;
        this.spriteTop = spriteTop;
    }

    public static EntityBounds fromFeet(EntityDef def, float feetX, float feetY, int spriteWidthPx, int spriteHeightPx) {
        float halfMoveW = def.moveWidthCells() / 2f;
        float moveLeft = feetX - halfMoveW;
        float moveRight = feetX + halfMoveW;
        float moveBottom = feetY;
        float moveTop = feetY + def.moveHeightCells();

        float spriteW = spriteWidthPx / (float) Constants.CELL_SIZE_PX;
        float spriteH = spriteHeightPx / (float) Constants.CELL_SIZE_PX;
        float halfSpriteW = spriteW / 2f;
        float spriteLeft = feetX - halfSpriteW;
        float spriteRight = feetX + halfSpriteW;
        float spriteBottom = feetY;
        float spriteTop = feetY + spriteH;

        return new EntityBounds(
                moveLeft, moveRight, moveBottom, moveTop, spriteLeft, spriteRight, spriteBottom, spriteTop);
    }

    public float moveCenterX() {
        return (moveLeft + moveRight) / 2f;
    }

    public float moveCenterY() {
        return (moveBottom + moveTop) / 2f;
    }
}
