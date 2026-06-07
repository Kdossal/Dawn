package com.dawn.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.entity.EntityDef;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityRegistry;
import com.dawn.entity.sprite.EntityAnimDef;
import com.dawn.entity.sprite.EntityAnimRegistry;
import com.dawn.entity.sprite.EntitySpriteSheet;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemId;
import com.dawn.item.ItemRegistry;
import com.dawn.render.SpriteAlphaMask;
import com.dawn.world.block.autotile.AutotileAtlas;
import com.dawn.world.block.autotile.AutotileCell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Loads per-file PNGs from {@code assets/} (Gradle run working directory). */
public final class DawnAssets implements Disposable {
    private final Map<String, TextureRegion> tiles = new HashMap<>();
    private final Map<String, TextureRegion> items = new HashMap<>();
    private final Map<String, TextureRegion> entities = new HashMap<>();
    private final Map<String, TextureRegion> ui = new HashMap<>();

    public final TextureRegion player;
    public final TextureRegion whitePixel;

    /** Shared slot chrome (hotbar + inventory + equipment). */
    public final UiCommon uiCommon;
    /** Inventory overlay chrome, grid, tabs. */
    public final UiInventory uiInventory;
    /** Equipment tab sheet (stats, XP bar). */
    public final UiEquipment uiEquipment;
    public final OcclusionMasks occlusionMasks;
    private final AutotileAtlas autotileAtlas;
    private final EntitySpriteSheet entitySpriteSheet;

    private final List<Texture> textures = new ArrayList<>();

    public DawnAssets() {
        occlusionMasks = new OcclusionMasks();
        for (BlockTextureId id : BlockTextureId.values()) {
            String path = "tiles/" + id.fileName + ".png";
            if (id.needsOcclusionMask()) {
                LoadedSprite loaded = loadWithOcclusionMask(path);
                tiles.put(id.fileName, loaded.region);
                occlusionMasks.registerBlock(id, loaded.mask);
            } else {
                tiles.put(id.fileName, load(path));
            }
        }
        autotileAtlas = AutotileAtlas.build(tiles);
        for (ItemId id : ItemRegistry.allIds()) {
            ItemDef def = ItemRegistry.get(id);
            items.put(def.iconId(), load("items/" + def.iconId() + ".png"));
        }
        for (EntityId id : EntityId.values()) {
            EntityDef def = EntityRegistry.get(id);
            String path = "entities/" + def.spriteId() + ".png";
            if (id == EntityId.PLAYER) {
                LoadedSprite loaded = loadPlayerSheet(path);
                entities.put(def.spriteId(), loaded.region);
                occlusionMasks.registerPlayer(loaded.mask);
            } else {
                entities.put(def.spriteId(), load(path));
            }
        }
        entitySpriteSheet = EntitySpriteSheet.build(entities);
        player = entityFrame(EntityId.PLAYER, 0, 0);
        whitePixel = createWhitePixel();

        uiCommon = new UiCommon(this);
        uiInventory = new UiInventory(this);
        uiEquipment = new UiEquipment(this);
    }

    TextureRegion loadUi(String path) {
        TextureRegion region = load("ui/" + path + ".png");
        ui.put(path, region);
        return region;
    }

    private TextureRegion createWhitePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        return new TextureRegion(texture);
    }

    private TextureRegion load(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(texture);
        return new TextureRegion(texture);
    }

    /** Single disk read: texture for rendering + boolean mask for occlusion tests. */
    private LoadedSprite loadPlayerSheet(String path) {
        EntityAnimDef animDef = EntityAnimRegistry.get(EntityId.PLAYER);
        if (animDef == null) {
            throw new IllegalStateException("Missing animation definition for PLAYER");
        }
        Pixmap pixmap = new Pixmap(Gdx.files.internal(path));
        try {
            int expectedW = animDef.cols() * animDef.frameWidth();
            int expectedH = animDef.rows() * animDef.frameHeight();
            if (pixmap.getWidth() != expectedW || pixmap.getHeight() != expectedH) {
                throw new IllegalStateException(
                        "Player sheet "
                                + path
                                + " expected "
                                + expectedW
                                + "x"
                                + expectedH
                                + " but was "
                                + pixmap.getWidth()
                                + "x"
                                + pixmap.getHeight());
            }
            SpriteAlphaMask mask =
                    SpriteAlphaMask.fromPixmapRegion(pixmap, 0, 0, animDef.frameWidth(), animDef.frameHeight());
            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            textures.add(texture);
            return new LoadedSprite(new TextureRegion(texture), mask);
        } finally {
            pixmap.dispose();
        }
    }

    private LoadedSprite loadWithOcclusionMask(String path) {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(path));
        try {
            SpriteAlphaMask mask = SpriteAlphaMask.fromPixmap(pixmap);
            Texture texture = new Texture(pixmap);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            textures.add(texture);
            return new LoadedSprite(new TextureRegion(texture), mask);
        } finally {
            pixmap.dispose();
        }
    }

    private record LoadedSprite(TextureRegion region, SpriteAlphaMask mask) {}

    public TextureRegion tile(BlockTextureId id) {
        return id == null ? null : tiles.get(id.fileName);
    }

    public TextureRegion autotileRegion(BlockTextureId sheet, AutotileCell cell) {
        return cell == null ? null : autotileRegion(sheet, cell.col(), cell.row());
    }

    public TextureRegion autotileRegion(BlockTextureId sheet, int col, int row) {
        return autotileAtlas.region(sheet, col, row);
    }

    public TextureRegion item(String iconId) {
        return iconId == null ? null : items.get(iconId);
    }

    public TextureRegion entity(String spriteId) {
        return spriteId == null ? null : entities.get(spriteId);
    }

    public TextureRegion textureForEntity(EntityDef def) {
        return def == null ? null : entity(def.spriteId());
    }

    public TextureRegion entityFrame(EntityId entityId, int col, int row) {
        return entitySpriteSheet.frame(entityId, col, row);
    }

    public TextureRegion ui(String path) {
        return ui.get(path);
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
        textures.clear();
        tiles.clear();
        items.clear();
        entities.clear();
        ui.clear();
    }

    /** {@code assets/ui/common/} */
    public static final class UiCommon {
        public final TextureRegion slot;
        public final TextureRegion slotSelected;
        public final TextureRegion slotEquip;
        public final TextureRegion leftClick;
        public final TextureRegion rightClick;

        UiCommon(DawnAssets assets) {
            slot = assets.loadUi("common/slot");
            slotSelected = assets.loadUi("common/slot_selected");
            slotEquip = assets.loadUi("common/slot_equip");
            leftClick = assets.loadUi("common/left_click");
            rightClick = assets.loadUi("common/right_click");
        }
    }

    /** {@code assets/ui/inventory/} */
    public static final class UiInventory {
        public final TextureRegion chromeBg;
        public final TextureRegion gridPanel;
        public final TextureRegion tabPage;
        public final TextureRegion tabEquipment;
        public final TextureRegion tabEquipmentActive;
        public final TextureRegion tabCrafting;
        public final TextureRegion tabCraftingActive;
        public final TextureRegion hotbarRow;

        UiInventory(DawnAssets assets) {
            chromeBg = assets.loadUi("inventory/chrome_bg");
            gridPanel = assets.loadUi("inventory/grid_panel");
            tabPage = assets.loadUi("inventory/tab_page");
            tabEquipment = assets.loadUi("inventory/tab_equipment");
            tabEquipmentActive = assets.loadUi("inventory/tab_equipment_active");
            tabCrafting = assets.loadUi("inventory/tab_crafting");
            tabCraftingActive = assets.loadUi("inventory/tab_crafting_active");
            hotbarRow = assets.loadUi("inventory/hotbar_row");
        }
    }

    /** {@code assets/ui/equipment/} */
    public static final class UiEquipment {
        public final TextureRegion statCell;
        public final TextureRegion expBarBg;
        public final TextureRegion expBarFill;

        UiEquipment(DawnAssets assets) {
            statCell = assets.loadUi("equipment/stat_cell");
            expBarBg = assets.loadUi("equipment/exp_bar_bg");
            expBarFill = assets.loadUi("equipment/exp_bar_fill");
        }
    }
}
