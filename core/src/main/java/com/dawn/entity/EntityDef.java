package com.dawn.entity;

public record EntityDef(
        EntityId id,
        String displayName,
        String spriteId,
        float moveWidthCells,
        float moveHeightCells,
        Stats defaultStats) {}
