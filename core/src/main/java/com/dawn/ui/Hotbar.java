package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.assets.DawnAssets;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.InventoryConstants;
import com.dawn.inventory.PlayerInventory;
import com.dawn.item.ItemStack;
import com.dawn.ui.inventory.InventorySlotRef;

/** Always-visible HUD row for every inventory slot (flat index 0..{@link InventoryConstants#SIZE}-1). */
public class Hotbar implements Disposable {
    private static final int SLOT_COUNT = HudSlotDesign.SLOT_COUNT;

    private static final int[] HOTKEYS = {
        Input.Keys.NUM_1,
        Input.Keys.NUM_2,
        Input.Keys.NUM_3,
        Input.Keys.NUM_4,
        Input.Keys.NUM_5,
        Input.Keys.NUM_6,
        Input.Keys.NUM_7,
        Input.Keys.NUM_8,
        Input.Keys.NUM_9,
        Input.Keys.NUM_0
    };

    private final DawnAssets assets;
    private final PlayerInventory inventory;
    private final Group root;
    private final HudDragSlot[] slots = new HudDragSlot[SLOT_COUNT];
    private final Rectangle bounds = new Rectangle();
    private Stage stage;

    public Hotbar(DawnAssets assets, DawnFonts fonts, PlayerInventory inventory) {
        this.assets = assets;
        this.inventory = inventory;
        root = new Group();
        for (int i = 0; i < SLOT_COUNT; i++) {
            HudDragSlot slot =
                    new HudDragSlot(assets, fonts, InventorySlotRef.grid(i), HudSlotChrome.HOTBAR);
            slots[i] = slot;
            root.addActor(slot);
        }
        layout();
    }

    public void attachStage(Stage stage) {
        if (this.stage == stage) {
            return;
        }
        root.remove();
        this.stage = stage;
        stage.addActor(root);
        root.toBack();
        layout();
    }

    public HudDragSlot slotAt(int index) {
        return slots[index];
    }

    public void layout() {
        float slotPx = HudSlotDesign.slotPx();
        float iconPx = HudSlotDesign.iconPx();
        HudSlotDesign.barBounds(bounds);
        root.setPosition(bounds.x, bounds.y);
        for (int i = 0; i < SLOT_COUNT; i++) {
            HudDragSlot slot = slots[i];
            slot.setLayoutSize(slotPx, iconPx);
            slot.setPosition(HudSlotDesign.slotX(i) - bounds.x, 0f);
        }
    }

    public void refreshSlots(EquipmentInventory equipment) {
        int selected = inventory.getSelectedIndex();
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots[i].setSelected(i == selected);
            slots[i].refresh(inventory, equipment, assets);
        }
    }

    public void update(boolean lockSelection) {
        if (lockSelection) {
            return;
        }
        for (int i = 0; i < HOTKEYS.length; i++) {
            if (i < SLOT_COUNT && Gdx.input.isKeyJustPressed(HOTKEYS[i])) {
                inventory.setSelectedIndex(i);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
            inventory.cycleSelectedIndex(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
            inventory.cycleSelectedIndex(1);
        }
    }

    public void applyScroll(float amountY, boolean lockSelection) {
        if (lockSelection) {
            return;
        }
        if (amountY > 0f) {
            inventory.cycleSelectedIndex(-1);
        } else if (amountY < 0f) {
            inventory.cycleSelectedIndex(1);
        }
    }

    public boolean handleClick(float screenX, float screenY) {
        Integer slotIndex = hitTest(screenX, screenY);
        if (slotIndex != null) {
            inventory.setSelectedIndex(slotIndex);
            return true;
        }
        return false;
    }

    public Integer hitTest(float screenX, float screenY) {
        layout();
        if (!bounds.contains(screenX, screenY)) {
            return null;
        }
        float relX = screenX - bounds.x;
        float step = HudSlotDesign.slotPx() + HudSlotDesign.gapPx();
        int index = (int) (relX / step);
        if (index < 0 || index >= SLOT_COUNT) {
            return null;
        }
        float inSlot = relX - index * step;
        if (inSlot > HudSlotDesign.slotPx()) {
            return null;
        }
        return index;
    }

    public ItemStack getHeld() {
        return inventory.getHeld();
    }

    @Override
    public void dispose() {
        root.remove();
    }
}
