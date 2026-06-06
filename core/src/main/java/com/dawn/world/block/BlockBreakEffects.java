package com.dawn.world.block;

import com.dawn.world.World;
import java.util.Optional;

/** Data-driven break/dig outcomes for non-structure cells. */
public final class BlockBreakEffects {
    private BlockBreakEffects() {}

    /** Clears an object-layer cell after mining/chopping. Returns a player message if handled. */
    public static Optional<String> breakObjectLayer(World world, int x, int y, BlockId id) {
        BlockDefinitions.BlockDef def = BlockDefinitions.get(id);
        if (def == null || def.layer() != Layer.OBJECT || !def.breakable()) {
            return Optional.empty();
        }
        BlockId stump = TreeBlocks.stumpFor(id);
        if (stump != null) {
            world.setObject(x, y, stump);
            return Optional.of("Removed tree");
        }
        world.setObject(x, y, BlockId.AIR);
        return Optional.of(breakMessage(id));
    }

    /** Removes a floor overlay (e.g. grass). Ground underneath is unchanged. */
    public static Optional<String> digFloor(World world, int x, int y, BlockId id) {
        if (id != BlockId.GRASS) {
            return Optional.empty();
        }
        world.setFloor(x, y, BlockId.AIR);
        return Optional.of("Dug grass");
    }

    /** Clears solid/water ground down to pit. */
    public static Optional<String> digGround(World world, int x, int y, BlockId id) {
        return switch (id) {
            case DIRT, SAND, STONE -> {
                world.setFloor(x, y, BlockId.AIR);
                world.setGround(x, y, BlockId.PIT);
                yield Optional.of("Dug ground");
            }
            case WATER -> {
                world.setFloor(x, y, BlockId.AIR);
                world.setGround(x, y, BlockId.PIT);
                yield Optional.of("Drained water");
            }
            default -> Optional.empty();
        };
    }

    private static String breakMessage(BlockId id) {
        return switch (id) {
            case ROCK -> "Mined rock";
            case BUSH -> "Cleared bush";
            case OAK_STUMP, SPRUCE_STUMP -> "Chopped stump";
            case CRATE -> "Broke crate";
            default -> "Broke block";
        };
    }
}
