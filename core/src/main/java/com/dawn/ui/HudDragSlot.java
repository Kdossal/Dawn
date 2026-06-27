package com.dawn.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.dawn.assets.DawnAssets;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.EquipmentSlot;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemStack;
import com.dawn.ui.inventory.InventorySlotRef;
import com.dawn.world.storage.CrateStorage;

/** HUD-sized slot hit target wrapping {@link HudItemSlot}. */
public final class HudDragSlot extends Group {
    private final InventorySlotRef slotRef;
    private final HudItemSlot slot;

    public HudDragSlot(DawnAssets assets, DawnFonts fonts, InventorySlotRef slotRef, HudSlotChrome chrome) {
        this.slotRef = slotRef;
        setTouchable(Touchable.enabled);
        slot = new HudItemSlot(assets, fonts, chrome);
        addActor(slot);
    }

    public InventorySlotRef slotRef() {
        return slotRef;
    }

    public void setLayoutSize(float slotPx, float iconPx) {
        setSize(slotPx, slotPx);
        slot.setLayoutSize(slotPx, iconPx);
    }

    public void setSelected(boolean selected) {
        slot.setSelected(selected);
    }

    public void refresh(PlayerInventory inventory, EquipmentInventory equipment, DawnAssets assets) {
        refresh(inventory, equipment, null, assets);
    }

    public void refresh(
            PlayerInventory inventory,
            EquipmentInventory equipment,
            CrateStorage container,
            DawnAssets assets) {
        ItemStack stack = stackAt(inventory, equipment, container);
        slot.refresh(stack, stack.isEmpty() ? guideIcon(assets) : null);
    }

    private ItemStack stackAt(
            PlayerInventory inventory, EquipmentInventory equipment, CrateStorage container) {
        return switch (slotRef.kind) {
            case GRID -> inventory.getSlotAtIndex(slotRef.gridIndex);
            case EQUIPMENT -> equipment.get(slotRef.equipmentSlot);
            case CONTAINER ->
                    container == null ? ItemStack.empty() : container.getSlotAtIndex(slotRef.gridIndex);
        };
    }

    private TextureRegion guideIcon(DawnAssets assets) {
        if (slotRef.kind != InventorySlotRef.Kind.EQUIPMENT) {
            return null;
        }
        EquipmentSlot equipSlot = slotRef.equipmentSlot;
        return switch (equipSlot) {
            case HEAD -> assets.uiCommon.headIcon;
            case CHEST -> assets.uiCommon.bodyIcon;
            case PANTS -> assets.uiCommon.legIcon;
            case BOOTS -> assets.uiCommon.feetIcon;
            case ACCESSORY_1, ACCESSORY_2, ACCESSORY_3, ACCESSORY_4 -> assets.uiCommon.accessoryIcon;
            case OFF_HAND -> assets.uiCommon.handIcon;
        };
    }
}
