package com.dawn.entity;

/** Attribute base values plus modifier deltas (equipment, buffs, etc.). */
public final class Stats {
    private final int[] base = new int[AttributeId.ALL.length];
    private final int[] modifiers = new int[AttributeId.ALL.length];

    public Stats() {}

    public Stats(Stats template) {
        if (template == null) {
            return;
        }
        System.arraycopy(template.base, 0, base, 0, base.length);
        System.arraycopy(template.modifiers, 0, modifiers, 0, modifiers.length);
    }

    public int get(AttributeId id) {
        int index = id.ordinal();
        return base[index] + modifiers[index];
    }

    public int getBase(AttributeId id) {
        return base[id.ordinal()];
    }

    public int getModifier(AttributeId id) {
        return modifiers[id.ordinal()];
    }

    public void setBase(AttributeId id, int value) {
        base[id.ordinal()] = value;
    }

    /** Clamps to {@link AttributeId#PLAYER_MIN}–{@link AttributeId#PLAYER_MAX}. */
    public void setPlayerBase(AttributeId id, int value) {
        setBase(id, clampPlayer(value));
    }

    public void addModifier(AttributeId id, int delta) {
        modifiers[id.ordinal()] += delta;
    }

    public void setAllBase(int value) {
        for (AttributeId id : AttributeId.ALL) {
            setBase(id, value);
        }
    }

    public static Stats withUniformBase(int value) {
        Stats stats = new Stats();
        stats.setAllBase(value);
        return stats;
    }

    public static int clampPlayer(int value) {
        return Math.max(AttributeId.PLAYER_MIN, Math.min(AttributeId.PLAYER_MAX, value));
    }
}
