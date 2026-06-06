package com.dawn.world.structure;

/**
 * How breaking one cell of a multi-part structure affects the rest.
 *
 * <ul>
 *   <li>{@link #PRIORITY_BY_ORDER} — break the lowest-order part that still exists.
 *   <li>{@link #BREAK_ENTIRE_STRUCTURE} — any part break removes every part (bed, house).
 * </ul>
 */
public enum StructureBreakPolicy {
    PRIORITY_BY_ORDER,
    BREAK_ENTIRE_STRUCTURE
}
