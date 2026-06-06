package com.dawn.world.structure;

import com.dawn.entity.Entity;
import com.dawn.gameplay.BreakTarget;
import com.dawn.world.CellPos;
import com.dawn.world.World;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import com.dawn.world.block.SurfaceRules;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-cell structures (trees, beds, buildings). Gameplay cells stay on the grid; this registry
 * coordinates break order and grouped removal.
 */
public final class StructureRegistry {
    private final Map<Long, Long> cellToInstance = new HashMap<>();
    private final Map<Long, StructureInstance> instances = new HashMap<>();
    private long nextId = 1L;

    /** Places a structure when {@link #canPlaceAt} would succeed. Returns false if placement is invalid. */
    public boolean place(World world, StructureKind kind, int anchorX, int anchorY, Entity entity) {
        if (!canPlaceAt(world, kind, anchorX, anchorY, entity)) {
            return false;
        }
        long id = nextId++;
        StructureInstance instance = new StructureInstance(id, kind, anchorX, anchorY);
        instances.put(id, instance);
        for (StructurePartDef part : kind.parts()) {
            int px = anchorX + part.dx();
            int py = anchorY + part.dy();
            world.setObject(px, py, part.blockId());
            cellToInstance.put(CellPos.pack(px, py), id);
        }
        return true;
    }

    public StructureInstance getAt(int x, int y) {
        Long id = cellToInstance.get(CellPos.pack(x, y));
        return id == null ? null : instances.get(id);
    }

    /** True if every part cell can receive a new structure (empty blocks, in bounds, clear of player). */
    public boolean canPlaceAt(World world, StructureKind kind, int anchorX, int anchorY, Entity entity) {
        if (!fits(world, kind, anchorX, anchorY)) {
            return false;
        }
        for (StructurePartDef part : kind.parts()) {
            int x = anchorX + part.dx();
            int y = anchorY + part.dy();
            if (!SurfaceRules.canPlaceObject(world, entity, x, y)) {
                return false;
            }
            if (getAt(x, y) != null) {
                return false;
            }
            if (entity != null && entity.occupiesCell(x, y)) {
                return false;
            }
        }
        return true;
    }

    /** Which cell/block should mining use when the player targets a structure cell. */
    public BreakTarget resolveBreakTarget(World world, int clickX, int clickY) {
        StructureInstance instance = getAt(clickX, clickY);
        if (instance == null) {
            return null;
        }
        StructurePartDef partToBreak = selectPartToBreak(world, instance);
        if (partToBreak == null) {
            return null;
        }
        int x = instance.anchorX() + partToBreak.dx();
        int y = instance.anchorY() + partToBreak.dy();
        BlockDefinitions.BlockDef def = BlockDefinitions.get(partToBreak.blockId());
        if (def == null || !def.breakable()) {
            return null;
        }
        return new BreakTarget(x, y, Layer.OBJECT, partToBreak.blockId(), def.breakHealth());
    }

    public StructureBreakResult breakPart(World world, int clickX, int clickY) {
        StructureInstance instance = getAt(clickX, clickY);
        if (instance == null) {
            return null;
        }
        return switch (instance.kind().breakPolicy()) {
            case PRIORITY_BY_ORDER -> breakPriorityPart(world, instance);
            case BREAK_ENTIRE_STRUCTURE -> breakEntireStructure(world, instance);
        };
    }

    private StructureBreakResult breakPriorityPart(World world, StructureInstance instance) {
        StructurePartDef part = selectPartToBreak(world, instance);
        if (part == null) {
            return null;
        }
        int x = instance.anchorX() + part.dx();
        int y = instance.anchorY() + part.dy();
        world.setObject(x, y, BlockId.AIR);
        cellToInstance.remove(CellPos.pack(x, y));

        if (!anyPartRemaining(world, instance)) {
            removeInstance(instance);
        }

        return new StructureBreakResult(
                List.of(new StructureBreakResult.PartBreak(x, y, Layer.OBJECT, part.blockId())),
                instance.kind().breakMessageFor(part.blockId()));
    }

    private StructureBreakResult breakEntireStructure(World world, StructureInstance instance) {
        ArrayList<StructureBreakResult.PartBreak> breaks = new ArrayList<>();
        for (StructurePartDef part : instance.kind().parts()) {
            int x = instance.anchorX() + part.dx();
            int y = instance.anchorY() + part.dy();
            if (world.getObject(x, y) != BlockId.AIR) {
                world.setObject(x, y, BlockId.AIR);
                cellToInstance.remove(CellPos.pack(x, y));
                breaks.add(new StructureBreakResult.PartBreak(x, y, Layer.OBJECT, part.blockId()));
            }
        }
        removeInstance(instance);
        String message =
                breaks.isEmpty()
                        ? "Broke structure"
                        : instance.kind().breakMessageFor(breaks.get(0).blockId());
        return new StructureBreakResult(breaks, message);
    }

    private static boolean anyPartRemaining(World world, StructureInstance instance) {
        for (StructurePartDef p : instance.kind().parts()) {
            int px = instance.anchorX() + p.dx();
            int py = instance.anchorY() + p.dy();
            if (world.getObject(px, py) != BlockId.AIR) {
                return true;
            }
        }
        return false;
    }

    private StructurePartDef selectPartToBreak(World world, StructureInstance instance) {
        StructurePartDef chosen = null;
        for (StructurePartDef part : instance.kind().parts()) {
            int x = instance.anchorX() + part.dx();
            int y = instance.anchorY() + part.dy();
            if (!world.inBounds(x, y) || world.getObject(x, y) != part.blockId()) {
                continue;
            }
            if (chosen == null || part.breakOrder() < chosen.breakOrder()) {
                chosen = part;
            }
        }
        return chosen;
    }

    private void removeInstance(StructureInstance instance) {
        instances.remove(instance.id());
        for (StructurePartDef part : instance.kind().parts()) {
            cellToInstance.remove(CellPos.pack(instance.anchorX() + part.dx(), instance.anchorY() + part.dy()));
        }
    }

    private static boolean fits(World world, StructureKind kind, int anchorX, int anchorY) {
        for (StructurePartDef part : kind.parts()) {
            if (!world.inBounds(anchorX + part.dx(), anchorY + part.dy())) {
                return false;
            }
        }
        return true;
    }
}
