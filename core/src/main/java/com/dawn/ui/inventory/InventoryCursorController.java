package com.dawn.ui.inventory;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.dawn.entity.Entity;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.world.storage.CrateStorage;

/** Click-to-grab inventory cursor (LMB full stack, RMB half / place one). */
public final class InventoryCursorController {
    private final PlayerInventory inventory;
    private final EquipmentInventory equipment;
    private final DropSystem dropSystem;
    private final Entity entity;
    private final Runnable onChanged;

    private ItemStack cursor = ItemStack.empty();
    private InventorySlotRef cursorOrigin;
    private boolean cursorFromCraft;
    private CrateStorage container;

    public InventoryCursorController(
            PlayerInventory inventory,
            EquipmentInventory equipment,
            DropSystem dropSystem,
            Entity entity,
            Runnable onChanged) {
        this.inventory = inventory;
        this.equipment = equipment;
        this.dropSystem = dropSystem;
        this.entity = entity;
        this.onChanged = onChanged;
    }

    public ItemStack cursorStack() {
        return cursor.copy();
    }

    public boolean hasCursor() {
        return !cursor.isEmpty();
    }

    public boolean hasCraftCursor() {
        return cursorFromCraft && hasCursor();
    }

    /** Craft output: no slot origin; merges with an existing craft cursor stack of the same item. */
    public void receiveCraftedStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (cursorFromCraft && !cursor.isEmpty() && cursor.itemId == stack.itemId) {
            cursor = cursor.withCount(cursor.count + stack.count);
        } else {
            cursor = stack.copy();
            cursorOrigin = null;
            cursorFromCraft = true;
        }
        notifyChanged();
    }

    public void dropCraftCursorToWorld() {
        if (!hasCraftCursor()) {
            return;
        }
        dropCursorToWorld();
    }

    public InventorySlotRef cursorOrigin() {
        return cursorOrigin;
    }

    public boolean isCursorFromContainer() {
        return cursorOrigin != null && cursorOrigin.kind == InventorySlotRef.Kind.CONTAINER;
    }

    public void setContainer(CrateStorage container) {
        this.container = container;
    }

    public CrateStorage container() {
        return container;
    }

    public void registerSlot(ItemSlotWidget widget) {
        registerDragTarget(widget, widget.slotRef());
    }

    public void registerDragTarget(Actor actor, InventorySlotRef ref) {
        actor.addListener(
                new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        if (button == Input.Buttons.LEFT) {
                            onSlotClick(ref, true);
                            event.stop();
                            return true;
                        }
                        if (button == Input.Buttons.RIGHT) {
                            onSlotClick(ref, false);
                            event.stop();
                            return true;
                        }
                        return false;
                    }
                });
    }

    public void registerWorldDropTarget(Actor actor) {
        actor.addListener(
                new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        if (button == Input.Buttons.LEFT) {
                            dropCursorToWorld();
                            event.stop();
                            return true;
                        }
                        return false;
                    }
                });
    }

    public void dropCursorToWorld() {
        if (cursor.isEmpty()) {
            return;
        }
        dropSystem.spawnPlayerDrop(cursor.copy(), entity.getX(), entity.getY());
        clearCursor();
        notifyChanged();
    }

    public ItemStack extractFromCursor(int amount) {
        if (cursor.isEmpty() || amount <= 0) {
            return ItemStack.empty();
        }
        int extracted = Math.min(amount, cursor.count);
        ItemStack out = ItemStack.of(cursor.itemId, extracted);
        reduceCursor(extracted);
        if (cursor.isEmpty()) {
            clearCursorOrigin();
        }
        notifyChanged();
        return out;
    }

    /** Returns held stack to origin, empty grid, merge slot, or world. */
    public void returnCursorToInventory() {
        if (cursor.isEmpty()) {
            return;
        }
        if (cursorOrigin != null && tryPlaceOnSlot(cursorOrigin, cursor.count)) {
            clearCursor();
            notifyChanged();
            return;
        }
        for (int i = 0; i < InventoryConstants.SIZE; i++) {
            InventorySlotRef ref = InventorySlotRef.grid(i);
            if (stackAt(ref).isEmpty() && tryPlaceOnSlot(ref, cursor.count)) {
                clearCursor();
                notifyChanged();
                return;
            }
        }
        for (int i = 0; i < InventoryConstants.SIZE; i++) {
            InventorySlotRef ref = InventorySlotRef.grid(i);
            if (tryMergeIntoSlot(ref, cursor.count)) {
                if (cursor.isEmpty()) {
                    notifyChanged();
                    return;
                }
            }
        }
        dropCursorToWorld();
    }

    void onSlotClick(InventorySlotRef ref, boolean fullStack) {
        ItemStack slotStack = stackAt(ref);

        if (cursor.isEmpty()) {
            if (slotStack.isEmpty()) {
                return;
            }
            int amount = fullStack ? slotStack.count : halfStackCount(slotStack.count);
            pickUp(ref, amount);
            notifyChanged();
            return;
        }

        if (sameSlot(ref, cursorOrigin) && slotStack.isEmpty() && fullStack) {
            tryPlaceOnSlot(ref, cursor.count);
            if (cursor.isEmpty()) {
                clearCursorOrigin();
            }
            notifyChanged();
            return;
        }

        int placeAmount = fullStack ? cursor.count : 1;
        boolean changed = interactCursorWithSlot(ref, placeAmount, fullStack);
        if (changed) {
            if (cursor.isEmpty()) {
                clearCursorOrigin();
            }
            notifyChanged();
        }
    }

    private boolean interactCursorWithSlot(InventorySlotRef ref, int placeAmount, boolean fullStack) {
        if (ref.kind == InventorySlotRef.Kind.EQUIPMENT) {
            return interactCursorWithEquipment(ref.equipmentSlot, placeAmount, fullStack);
        }
        return interactCursorWithStorageSlot(ref, placeAmount, fullStack);
    }

    private boolean interactCursorWithStorageSlot(InventorySlotRef ref, int placeAmount, boolean fullStack) {
        ItemStack slotStack = stackAt(ref);

        if (cursorOrigin != null && cursorOrigin.kind == InventorySlotRef.Kind.EQUIPMENT) {
            return placeEquipCursorOnSlot(ref, fullStack);
        }

        if (slotStack.isEmpty()) {
            return tryPlaceOnSlot(ref, placeAmount);
        }

        if (cursor.itemId == slotStack.itemId) {
            return tryMergeIntoSlot(ref, placeAmount);
        }

        if (fullStack) {
            return swapWithSlot(ref);
        }
        if (tryMergeIntoSlot(ref, 1)) {
            return true;
        }
        return swapWithSlot(ref);
    }

    private boolean interactCursorWithGrid(int gridIndex, int placeAmount, boolean fullStack) {
        return interactCursorWithStorageSlot(InventorySlotRef.grid(gridIndex), placeAmount, fullStack);
    }

    private boolean interactCursorWithEquipment(EquipmentSlot slot, int placeAmount, boolean fullStack) {
        if (equipment == null) {
            return false;
        }
        InventorySlotRef ref = InventorySlotRef.equipment(slot);
        ItemStack slotStack = equipment.get(slot);

        if (!equipment.canEquip(cursor, slot)) {
            return false;
        }

        if (slotStack.isEmpty()) {
            int place = Math.min(equipmentPlaceCount(placeAmount), cursor.count);
            equipment.set(slot, cursor.withCount(place));
            reduceCursor(place);
            return true;
        }

        if (cursor.itemId == slotStack.itemId) {
            return false;
        }

        if (!slotStack.isEmpty()
                && cursorOrigin != null
                && cursorOrigin.kind == InventorySlotRef.Kind.EQUIPMENT
                && !equipment.canEquip(slotStack, cursorOrigin.equipmentSlot)) {
            return false;
        }

        return swapWithSlot(ref);
    }

    private boolean placeEquipCursorOnSlot(InventorySlotRef ref, boolean fullStack) {
        if (equipment == null || cursorOrigin == null) {
            return false;
        }
        EquipmentSlot equipSlot = cursorOrigin.equipmentSlot;
        ItemStack slotStack = stackAt(ref);

        if (slotStack.isEmpty()) {
            int place = fullStack ? cursor.count : 1;
            place = Math.min(place, cursor.count);
            writeStack(ref, cursor.withCount(place));
            reduceCursor(place);
            if (cursor.isEmpty()) {
                equipment.set(equipSlot, ItemStack.empty());
            }
            return true;
        }

        if (!equipment.canEquip(slotStack, equipSlot)) {
            return false;
        }

        ItemStack previous = slotStack.copy();
        writeStack(ref, cursor.copy());
        cursor = previous;
        equipment.set(equipSlot, ItemStack.empty());
        cursorOrigin = ref;
        cursorFromCraft = false;
        syncHotbarSelection(ref);
        return true;
    }

    private boolean placeEquipCursorOnGrid(int gridIndex, boolean fullStack) {
        return placeEquipCursorOnSlot(InventorySlotRef.grid(gridIndex), fullStack);
    }

    private void pickUp(InventorySlotRef ref, int amount) {
        ItemStack taken = extractFromSlot(ref, amount);
        if (taken.isEmpty()) {
            return;
        }
        if (cursor.isEmpty()) {
            cursor = taken;
            cursorOrigin = ref;
            cursorFromCraft = false;
            syncHotbarSelection(ref);
        } else if (cursor.itemId == taken.itemId) {
            cursor = cursor.withCount(cursor.count + taken.count);
        } else {
            writeStack(ref, taken);
        }
    }

    private boolean tryPlaceOnSlot(InventorySlotRef ref, int amount) {
        if (cursor.isEmpty() || amount <= 0) {
            return false;
        }
        ItemStack slotStack = stackAt(ref);

        if (ref.kind == InventorySlotRef.Kind.EQUIPMENT) {
            if (!equipment.canEquip(cursor, ref.equipmentSlot)) {
                return false;
            }
            if (!slotStack.isEmpty()) {
                return false;
            }
            int place = Math.min(equipmentPlaceCount(amount), cursor.count);
            equipment.set(ref.equipmentSlot, cursor.withCount(place));
            reduceCursor(place);
            return true;
        }

        if (slotStack.isEmpty()) {
            int place = Math.min(amount, cursor.count);
            writeStack(ref, cursor.withCount(place));
            reduceCursor(place);
            return true;
        }
        return tryMergeIntoSlot(ref, amount);
    }

    private boolean tryMergeIntoSlot(InventorySlotRef ref, int amount) {
        if (cursor.isEmpty() || amount <= 0) {
            return false;
        }
        if (ref.kind != InventorySlotRef.Kind.GRID && ref.kind != InventorySlotRef.Kind.CONTAINER) {
            return false;
        }
        ItemStack slotStack = stackAt(ref);
        if (slotStack.isEmpty() || cursor.itemId != slotStack.itemId) {
            return false;
        }

        ItemDef def = ItemRegistry.get(cursor.itemId);
        int space = def.maxStack() - slotStack.count;
        if (space <= 0) {
            return false;
        }
        int move = Math.min(Math.min(amount, space), cursor.count);
        writeStack(ref, slotStack.withCount(slotStack.count + move));
        reduceCursor(move);
        return move > 0;
    }

    private boolean swapWithSlot(InventorySlotRef ref) {
        ItemStack slotStack = stackAt(ref);

        if (ref.kind == InventorySlotRef.Kind.EQUIPMENT) {
            if (!equipment.canEquip(cursor, ref.equipmentSlot)) {
                return false;
            }
            if (!slotStack.isEmpty()
                    && cursorOrigin != null
                    && cursorOrigin.kind == InventorySlotRef.Kind.EQUIPMENT
                    && !equipment.canEquip(slotStack, cursorOrigin.equipmentSlot)) {
                return false;
            }
            ItemStack onCursor = cursor.copy();
            equipment.set(ref.equipmentSlot, onCursor.withCount(Math.min(1, onCursor.count)));
            cursor = slotStack.isEmpty() ? ItemStack.empty() : slotStack.copy();
            if (cursor.isEmpty()) {
                clearCursorOrigin();
            } else {
                cursorOrigin = ref;
                cursorFromCraft = false;
                syncHotbarSelection(ref);
            }
            return true;
        }

        ItemStack onCursor = cursor.copy();
        writeStack(ref, onCursor);
        cursor = slotStack.isEmpty() ? ItemStack.empty() : slotStack.copy();
        if (cursor.isEmpty()) {
            clearCursorOrigin();
        } else {
            cursorOrigin = ref;
            cursorFromCraft = false;
            syncHotbarSelection(ref);
        }
        return true;
    }

    private void syncHotbarSelection(InventorySlotRef ref) {
        if (ref != null && ref.kind == InventorySlotRef.Kind.GRID) {
            inventory.setSelectedIndex(ref.gridIndex);
        }
    }

    private ItemStack extractFromSlot(InventorySlotRef ref, int amount) {
        ItemStack s = stackAt(ref);
        if (s.isEmpty() || amount <= 0) {
            return ItemStack.empty();
        }
        int take = Math.min(amount, s.count);
        ItemStack taken = ItemStack.of(s.itemId, take);
        int left = s.count - take;
        writeStack(ref, left <= 0 ? ItemStack.empty() : s.withCount(left));
        return taken;
    }

    private void writeStack(InventorySlotRef ref, ItemStack stack) {
        if (ref.kind == InventorySlotRef.Kind.GRID) {
            inventory.setSlotAtIndex(ref.gridIndex, stack);
        } else if (ref.kind == InventorySlotRef.Kind.CONTAINER && container != null) {
            container.setSlotAtIndex(ref.gridIndex, stack);
        } else if (equipment != null && ref.kind == InventorySlotRef.Kind.EQUIPMENT) {
            equipment.set(ref.equipmentSlot, stack);
        }
    }

    private void reduceCursor(int amount) {
        if (amount <= 0) {
            return;
        }
        int left = cursor.count - amount;
        cursor = left <= 0 ? ItemStack.empty() : cursor.withCount(left);
    }

    private void clearCursor() {
        cursor = ItemStack.empty();
        clearCursorOrigin();
        cursorFromCraft = false;
    }

    private void clearCursorOrigin() {
        cursorOrigin = null;
    }

    private static int halfStackCount(int count) {
        return (count + 1) / 2;
    }

    private static int equipmentPlaceCount(int requested) {
        return Math.min(1, requested);
    }

    private static boolean sameSlot(InventorySlotRef a, InventorySlotRef b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.kind != b.kind) {
            return false;
        }
        return switch (a.kind) {
            case GRID, CONTAINER -> a.gridIndex == b.gridIndex;
            case EQUIPMENT -> a.equipmentSlot == b.equipmentSlot;
        };
    }

    private ItemStack stackAt(InventorySlotRef ref) {
        if (ref.kind == InventorySlotRef.Kind.GRID) {
            return inventory.getSlotAtIndex(ref.gridIndex);
        }
        if (ref.kind == InventorySlotRef.Kind.CONTAINER) {
            if (container == null) {
                return ItemStack.empty();
            }
            return container.getSlotAtIndex(ref.gridIndex);
        }
        if (equipment == null) {
            return ItemStack.empty();
        }
        return equipment.get(ref.equipmentSlot);
    }

    private void notifyChanged() {
        if (onChanged != null) {
            onChanged.run();
        }
    }
}
