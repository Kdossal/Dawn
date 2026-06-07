package com.dawn.entity.sprite;

import com.dawn.entity.EntityId;
import java.util.Map;

/** Sprite sheet and clip definitions for one animated entity. */
public record EntityAnimDef(
        EntityId entityId,
        String spriteId,
        int frameWidth,
        int frameHeight,
        int cols,
        int rows,
        Map<String, EntityAnimClip> clips) {

    public EntityAnimClip clip(String clipId) {
        EntityAnimClip clip = clips.get(clipId);
        if (clip == null) {
            throw new IllegalArgumentException("Missing entity animation clip \"" + clipId + "\" for " + entityId);
        }
        return clip;
    }
}
