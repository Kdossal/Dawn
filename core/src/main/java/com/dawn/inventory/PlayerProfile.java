package com.dawn.inventory;

import com.dawn.entity.Entity;
import com.dawn.entity.StatId;

/** Player progression and UI sheet data for the equipment tab. */
public final class PlayerProfile {
    public String name = "Traveler";
    public int level = 1;
    public int exp = 40;
    public int expToNext = 100;

    private Entity entity;

    public void bindEntity(Entity entity) {
        this.entity = entity;
    }

    public float expRatio() {
        return expToNext <= 0 ? 0f : Math.min(1f, exp / (float) expToNext);
    }

    public static final String[] STAT_NAMES = StatId.DISPLAY_NAMES;

    public int statValue(int index) {
        if (entity == null || index < 0 || index >= StatId.ALL.length) {
            return 0;
        }
        return entity.getStats().get(StatId.ALL[index]);
    }
}
