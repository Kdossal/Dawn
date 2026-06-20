package com.dawn.gameplay;

import com.dawn.gameplay.drops.DropSystem;
import com.dawn.gameplay.drops.LootTable;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.item.Placeable;
import com.dawn.item.PlaceableExecutor;
import com.dawn.entity.Entity;
import com.dawn.world.World;
import com.dawn.world.block.BlockBreakEffects;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.Layer;
import com.dawn.world.structure.StructureBreakResult;
import java.util.List;
import java.util.Optional;

public class InteractionSystem {
    private final LootTable lootTable;
    private final DropSystem dropSystem;
    private String lastMessage = "";

    public InteractionSystem(LootTable lootTable, DropSystem dropSystem) {
        this.lootTable = lootTable;
        this.dropSystem = dropSystem;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    void setMessage(String message) {
        lastMessage = message == null ? "" : message;
    }

    public boolean tryPlace(
            World world,
            Entity entity,
            PlayerInventory inventory,
            float playerX,
            float playerY,
            int x,
            int y,
            ItemStack held) {
        lastMessage = "";
        ItemDef def = held == null || held.isEmpty() ? null : ItemRegistry.get(held);
        if (def == null || !def.canPlace()) {
            lastMessage = "Nothing to place";
            return false;
        }

        PlacementRules.Result result =
                PlacementRules.evaluate(world, entity, playerX, playerY, held, x, y);
        if (result == null) {
            return false;
        }
        if (!result.valid()) {
            lastMessage = result.failureMessage() != null ? result.failureMessage() : "Can't place there";
            return false;
        }

        if (!PlaceableExecutor.apply(world, entity, result.placeable(), result.anchorX(), result.anchorY())) {
            lastMessage = PlaceableExecutor.placementError(result.placeable());
            return false;
        }

        ItemStack extracted = inventory.extractFromHeld(1);
        if (extracted.isEmpty()) {
            rollbackPlacement(world, result.placeable(), result.anchorX(), result.anchorY());
            return false;
        }

        lastMessage = "Placed " + def.displayName().toLowerCase();
        return true;
    }

    private static void rollbackPlacement(World world, Placeable placeable, int anchorX, int anchorY) {
        if (placeable instanceof Placeable.Ground) {
            world.setGround(anchorX, anchorY, BlockId.PIT);
        } else if (placeable instanceof Placeable.Block) {
            world.setObject(anchorX, anchorY, BlockId.AIR);
        } else if (placeable instanceof Placeable.Structure structure) {
            StructureBreakResult broken = world.getStructures().breakPart(world, anchorX, anchorY);
            if (broken == null) {
                for (var part : structure.kind().parts()) {
                    world.setObject(anchorX + part.dx(), anchorY + part.dy(), BlockId.AIR);
                }
            }
        }
    }

    void executeBreak(World world, int x, int y, Layer layer, BlockId id) {
        switch (layer) {
            case OBJECT -> breakObject(world, x, y, id);
            case FLOOR -> digFloor(world, x, y, id);
            case GROUND -> digGround(world, x, y, id);
        }
    }

    private void spawnLoot(World world, int x, int y, Layer layer, BlockId idBeforeBreak) {
        List<ItemStack> loot = lootTable.roll(new LootTable.DropContext(idBeforeBreak, layer));
        for (ItemStack stack : loot) {
            dropSystem.spawnAtCell(world, stack, x, y);
        }
    }

    private void breakObject(World world, int x, int y, BlockId id) {
        StructureBreakResult structureResult = world.getStructures().breakPart(world, x, y);
        if (structureResult != null) {
            lastMessage = structureResult.message();
            for (StructureBreakResult.PartBreak part : structureResult.partBreaks()) {
                spawnLoot(world, part.x(), part.y(), part.layer(), part.blockId());
            }
            return;
        }
        Optional<String> message = BlockBreakEffects.breakObjectLayer(world, x, y, id);
        if (message.isPresent()) {
            lastMessage = message.get();
            spawnLoot(world, x, y, Layer.OBJECT, id);
        }
    }

    private void digFloor(World world, int x, int y, BlockId id) {
        Optional<String> message = BlockBreakEffects.digFloor(world, x, y, id);
        if (message.isEmpty()) {
            return;
        }
        lastMessage = message.get();
    }

    private void digGround(World world, int x, int y, BlockId id) {
        Optional<String> message = BlockBreakEffects.digGround(world, x, y, id);
        if (message.isEmpty()) {
            return;
        }
        lastMessage = message.get();
        spawnLoot(world, x, y, Layer.GROUND, id);
    }
}
