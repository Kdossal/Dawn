package com.dawn.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.entity.Entity;
import com.dawn.entity.sprite.EntitySpriteFrame;
import com.dawn.gameplay.InteractResolver;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import com.dawn.ui.inventory.InventorySlotRef;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.storage.CrateStorage;

/** In-world 4×3 crate storage panel positioned via {@link WorldHudPopupPlacement}. */
public final class CrateStorageOverlay {
    private static final WorldHudPopupPlacement.Config PLACEMENT = WorldHudPopupPlacement.Config.crateStorage();

    private final DawnAssets assets;
    private final PlayerInventory inventory;
    private final EquipmentInventory equipment;
    private final World world;
    private final Stage stage;
    private final HudItemDragSession dragSession;

    private final Group root;
    private final Image panelBg;
    private final HudDragSlot[] slots = new HudDragSlot[CrateStorage.SLOT_COUNT];

    private int openCellX = -1;
    private int openCellY = -1;
    private boolean open;

    public CrateStorageOverlay(
            DawnFonts fonts,
            DawnAssets assets,
            PlayerInventory inventory,
            EquipmentInventory equipment,
            World world,
            Stage stage,
            HudItemDragSession dragSession) {
        this.assets = assets;
        this.inventory = inventory;
        this.equipment = equipment;
        this.world = world;
        this.stage = stage;
        this.dragSession = dragSession;

        int mult = Constants.HUD_ART_MULT;
        NinePatch patch =
                new NinePatch(
                        assets.uiInventory.panel,
                        HudPanelDesign.BASE_NINE_SLICE,
                        HudPanelDesign.BASE_NINE_SLICE,
                        HudPanelDesign.BASE_NINE_SLICE,
                        HudPanelDesign.BASE_NINE_SLICE);
        patch.scale(mult, mult);
        panelBg = new Image(new NinePatchDrawable(patch));
        panelBg.setTouchable(Touchable.disabled);

        root = new Group();
        root.setVisible(false);
        root.setTouchable(Touchable.childrenOnly);
        root.addActor(panelBg);

        int idx = 0;
        for (int row = 0; row < CrateStorageDesign.SLOT_ROWS; row++) {
            for (int col = 0; col < CrateStorageDesign.SLOT_COLS; col++) {
                HudDragSlot slot =
                        new HudDragSlot(
                                assets,
                                fonts,
                                InventorySlotRef.container(CrateStorage.toIndex(row, col)),
                                HudSlotChrome.DULL);
                dragSession.registerContainerSlot(slot);
                slots[idx++] = slot;
                root.addActor(slot);
            }
        }

        stage.addActor(root);
    }

    public boolean isOpen() {
        return open;
    }

    public int openCellX() {
        return openCellX;
    }

    public int openCellY() {
        return openCellY;
    }

    public void toggle(InteractResolver.InteractTarget target) {
        if (target == null) {
            return;
        }
        if (open && openCellX == target.cellX() && openCellY == target.cellY()) {
            close();
            return;
        }
        open(target.cellX(), target.cellY());
    }

    public void open(int cellX, int cellY) {
        if (world.getObject(cellX, cellY) != BlockId.CRATE) {
            return;
        }
        CrateStorage storage = world.getCrateStorage().getAt(cellX, cellY);
        if (storage == null) {
            storage = world.getCrateStorage().createAt(cellX, cellY);
        }
        openCellX = cellX;
        openCellY = cellY;
        open = true;
        root.setVisible(true);
        dragSession.setContainer(storage);
        refreshAll();
        root.toFront();
        dragSession.cursorActor().toFront();
    }

    public void close() {
        if (!open) {
            return;
        }
        open = false;
        openCellX = -1;
        openCellY = -1;
        dragSession.cursorController().returnCursorToInventory();
        root.setVisible(false);
        dragSession.clearContainer();
    }

    /** Block removed or storage cleared — spill container-origin cursor to world. */
    private void closeBecauseCrateUnavailable() {
        if (!open) {
            return;
        }
        open = false;
        openCellX = -1;
        openCellY = -1;
        var cursor = dragSession.cursorController();
        if (cursor.isCursorFromContainer()) {
            cursor.dropCursorToWorld();
        } else {
            cursor.returnCursorToInventory();
        }
        root.setVisible(false);
        dragSession.clearContainer();
    }

    public void refreshAll() {
        CrateStorage container = dragSession.container();
        for (HudDragSlot slot : slots) {
            slot.refresh(inventory, equipment, container, assets);
        }
        dragSession.refreshCursor();
    }

    public void update(Entity player, OrthographicCamera worldCamera) {
        if (!open) {
            return;
        }
        if (world.getObject(openCellX, openCellY) != BlockId.CRATE
                || world.getCrateStorage().getAt(openCellX, openCellY) == null) {
            closeBecauseCrateUnavailable();
            return;
        }
        if (!InteractResolver.isCrateInReach(world, player, openCellX, openCellY)) {
            close();
            return;
        }
        layoutAbovePlayer(player, worldCamera);
    }

    private void layoutAbovePlayer(Entity player, OrthographicCamera worldCamera) {
        CrateStorageDesign.Layout layout = CrateStorageDesign.layout();

        EntitySpriteFrame frame = player.resolveSpriteFrame(assets);
        float spriteW =
                frame != null ? frame.widthPx() : (float) Constants.PLAYER_SPRITE_WIDTH_PX;
        float spriteH =
                frame != null ? frame.heightPx() : (float) Constants.PLAYER_SPRITE_HEIGHT_PX;

        WorldHudPopupPlacement.Anchor anchor =
                WorldHudPopupPlacement.spriteTopCenter(
                        player.getX(), player.getY(), spriteW, spriteH, worldCamera);
        WorldHudPopupPlacement.PanelPosition panel =
                WorldHudPopupPlacement.clampToStage(
                        WorldHudPopupPlacement.panelAboveAnchor(anchor, layout.panelW(), PLACEMENT),
                        layout.panelW(),
                        layout.panelH());

        root.setSize(layout.panelW(), layout.panelH());
        root.setPosition(panel.panelX(), panel.panelBottomY());

        panelBg.setSize(layout.panelW(), layout.panelH());
        panelBg.setPosition(0f, 0f);

        Rectangle slotBounds = new Rectangle();
        int idx = 0;
        for (int row = 0; row < CrateStorageDesign.SLOT_ROWS; row++) {
            for (int col = 0; col < CrateStorageDesign.SLOT_COLS; col++) {
                CrateStorageDesign.slotBounds(0f, 0f, layout, col, row, slotBounds);
                HudDragSlot slot = slots[idx++];
                slot.setLayoutSize(layout.slotPx(), layout.iconPx());
                slot.setPosition(slotBounds.x, slotBounds.y);
            }
        }
    }
}
