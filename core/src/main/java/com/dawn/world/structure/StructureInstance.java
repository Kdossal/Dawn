package com.dawn.world.structure;

/** Runtime structure placed at an anchor cell. */
public final class StructureInstance {
    private final long id;
    private final StructureKind kind;
    private final int anchorX;
    private final int anchorY;

    StructureInstance(long id, StructureKind kind, int anchorX, int anchorY) {
        this.id = id;
        this.kind = kind;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    public long id() {
        return id;
    }

    public StructureKind kind() {
        return kind;
    }

    public int anchorX() {
        return anchorX;
    }

    public int anchorY() {
        return anchorY;
    }
}
