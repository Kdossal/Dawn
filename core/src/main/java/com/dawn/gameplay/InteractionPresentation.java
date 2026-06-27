package com.dawn.gameplay;

import com.dawn.entity.Entity;
import com.dawn.gameplay.ReachResolver;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.gameplay.placement.PlacementPreview;
import com.dawn.gameplay.placement.PlacementPreviewResolver;
import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;
import com.dawn.world.World;
import java.util.Collections;
import java.util.List;

/** Cached hover visuals computed during update, drawn during render. */
public final class InteractionPresentation {
    private List<PlacementPreview> placementPreviews = List.of();
    private List<InteractionHighlight.Highlight> breakHighlights = List.of();
    private boolean showPlacementGhosts;

    public List<PlacementPreview> placementPreviews() {
        return placementPreviews;
    }

    public List<InteractionHighlight.Highlight> breakHighlights() {
        return breakHighlights;
    }

    public boolean showPlacementGhosts() {
        return showPlacementGhosts;
    }

    public void update(
            World world,
            Entity player,
            ItemStack held,
            TargetCell target,
            boolean showPlacementGhost,
            boolean suppressBreakHighlights) {
        update(world, player, held, target, showPlacementGhost, suppressBreakHighlights, null);
    }

    public void update(
            World world,
            Entity player,
            ItemStack held,
            TargetCell target,
            boolean showPlacementGhost,
            boolean suppressBreakHighlights,
            Placeable placementOverride) {
        showPlacementGhosts = showPlacementGhost;
        if (target != null) {
            if (placementOverride != null) {
                placementPreviews =
                        PlacementPreviewResolver.resolve(
                                world,
                                player,
                                player.getX(),
                                player.getY(),
                                placementOverride,
                                ReachResolver.radiusCellsFloatForHeld(held),
                                target);
            } else {
                placementPreviews = PlacementPreviewResolver.resolve(
                        world, player, player.getX(), player.getY(), held, target);
            }
        } else {
            placementPreviews = List.of();
        }
        breakHighlights =
                target == null || suppressBreakHighlights
                        ? List.of()
                        : InteractionHighlight.resolve(
                                world, player, player.getX(), player.getY(), held, target);
    }

    public void update(
            World world,
            Entity player,
            ItemStack held,
            TargetCell target,
            boolean showPlacementGhost) {
        update(world, player, held, target, showPlacementGhost, false);
    }

    public void clear() {
        placementPreviews = Collections.emptyList();
        breakHighlights = Collections.emptyList();
    }

    public boolean hasValidPlacementPreview() {
        for (PlacementPreview preview : placementPreviews) {
            if (isValid(preview)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValid(PlacementPreview preview) {
        if (preview instanceof PlacementPreview.FloorCell cell) {
            return cell.valid();
        }
        if (preview instanceof PlacementPreview.BlockSprite block) {
            return block.valid();
        }
        if (preview instanceof PlacementPreview.StructureMask mask) {
            return mask.valid();
        }
        return false;
    }
}
