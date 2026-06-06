package com.dawn.entity;

/** Base stat values plus modifier deltas (equipment, buffs, etc.). */
public final class Stats {
    private final int[] base = new int[StatId.ALL.length];
    private final int[] modifiers = new int[StatId.ALL.length];

    public Stats() {}

    public Stats(Stats template) {
        if (template == null) {
            return;
        }
        System.arraycopy(template.base, 0, base, 0, base.length);
        System.arraycopy(template.modifiers, 0, modifiers, 0, modifiers.length);
    }

    public int get(StatId id) {
        int index = id.ordinal();
        return base[index] + modifiers[index];
    }

    public int getBase(StatId id) {
        return base[id.ordinal()];
    }

    public int getModifier(StatId id) {
        return modifiers[id.ordinal()];
    }

    public void setBase(StatId id, int value) {
        base[id.ordinal()] = value;
    }

    public void addModifier(StatId id, int delta) {
        modifiers[id.ordinal()] += delta;
    }

    public void setAllBase(int value) {
        for (StatId id : StatId.ALL) {
            setBase(id, value);
        }
    }

    public static Stats withUniformBase(int value) {
        Stats stats = new Stats();
        stats.setAllBase(value);
        return stats;
    }
}
