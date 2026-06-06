package com.dawn.gameplay;

import com.dawn.entity.Entity;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.gameplay.placement.PlacementPreview;
import com.dawn.gameplay.placement.PlacementPreviewResolver;
import com.dawn.item.ItemStack;
import com.dawn.world.World;
import java.util.Collections;
import java.util.List;

/** Cached hover visuals computed during update, drawn during render. */
public final class InteractionPresentation {
    private List<PlacementPreview> placementPreviews = List.of();
    private List<InteractionHighlight.Highlight> breakHighlights = List.of();

    public List<PlacementPreview> placementPreviews() {
        return placementPreviews;
    }

    public List<InteractionHighlight.Highlight> breakHighlights() {
        return breakHighlights;
    }

    public void update(
            World world,
            Entity player,
            ItemStack held,
            TargetCell target,
            boolean showPlacementGhost) {
        if (showPlacementGhost && target != null) {
            placementPreviews = PlacementPreviewResolver.resolve(
                    world, player, player.getX(), player.getY(), held, target);
        } else {
            placementPreviews = List.of();
        }
        breakHighlights =
                target == null
                        ? List.of()
                        : InteractionHighlight.resolve(
                                world, player, player.getX(), player.getY(), held, target);
    }

    public void clear() {
        placementPreviews = Collections.emptyList();
        breakHighlights = Collections.emptyList();
    }
}
