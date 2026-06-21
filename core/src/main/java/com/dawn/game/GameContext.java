package com.dawn.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.render.GameSettings;
import com.dawn.render.HudViewport;
import com.dawn.render.RenderSettings;
import com.dawn.render.ZoomController;
import com.dawn.entity.EntityId;
import com.dawn.entity.EntityManager;
import com.dawn.gameplay.EatSystem;
import com.dawn.gameplay.InteractionPresentation;
import com.dawn.gameplay.InteractionSystem;
import com.dawn.gameplay.MiningSystem;
import com.dawn.gameplay.PlacementSystem;
import com.dawn.gameplay.drops.DropRenderer;
import com.dawn.gameplay.drops.DropSystem;
import com.dawn.gameplay.drops.LootTable;
import com.dawn.gameplay.sim.SimulationSystem;
import com.dawn.input.InputController;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import com.dawn.inventory.PlayerProfile;
import com.dawn.ui.inventory.InventoryOverlay;
import com.dawn.ui.DawnFonts;
import com.dawn.ui.DebugOverlay;
import com.dawn.ui.EquipmentSidebarHud;
import com.dawn.ui.Hotbar;
import com.dawn.ui.HudAssets;
import com.dawn.ui.PauseOverlay;
import com.dawn.ui.StatusHud;
import com.dawn.ui.VitalsHud;
import com.dawn.world.World;
import com.dawn.world.render.WorldRenderer;

/** Runtime game state and systems wired for one play session. */
public final class GameContext implements Disposable {
    public final DawnAssets assets;
    public final SpriteBatch worldBatch;
    public final ShapeRenderer worldOverlay;
    public final World world;
    public final EntityManager entities;
    public final PlayerInventory inventory;
    public final EquipmentInventory equipment;
    public final PlayerProfile profile;
    public final InventoryOverlay inventoryOverlay;
    public final PauseOverlay pauseOverlay;
    public final WorldRenderer worldRenderer;
    public final InputController input;
    public final InteractionSystem interaction;
    public final InteractionPresentation interactionPresentation = new InteractionPresentation();
    public final MiningSystem mining;
    public final PlacementSystem placement;
    public final EatSystem eat;
    public final DropSystem dropSystem;
    public final DropRenderer dropRenderer;
    public final Hotbar hotbar;
    public final VitalsHud vitalsHud;
    public final StatusHud statusHud;
    public final EquipmentSidebarHud equipmentSidebar;
    public final DebugOverlay debug;
    public final RenderSettings renderSettings;
    public final GameSettings gameSettings;
    public final ZoomController zoomController;
    public final GameLoop gameLoop;
    public final DawnFonts fonts;
    public final HudAssets hud;

    private GameContext(
            DawnAssets assets,
            SpriteBatch worldBatch,
            ShapeRenderer worldOverlay,
            World world,
            EntityManager entities,
            PlayerInventory inventory,
            EquipmentInventory equipment,
            PlayerProfile profile,
            InventoryOverlay inventoryOverlay,
            PauseOverlay pauseOverlay,
            WorldRenderer worldRenderer,
            InputController input,
            InteractionSystem interaction,
            MiningSystem mining,
            PlacementSystem placement,
            EatSystem eat,
            DropSystem dropSystem,
            DropRenderer dropRenderer,
            Hotbar hotbar,
            VitalsHud vitalsHud,
            StatusHud statusHud,
            EquipmentSidebarHud equipmentSidebar,
            DebugOverlay debug,
            GameLoop gameLoop,
            HudAssets hud,
            DawnFonts fonts,
            GameSettings gameSettings,
            ZoomController zoomController,
            RenderSettings renderSettings) {
        this.assets = assets;
        this.worldBatch = worldBatch;
        this.worldOverlay = worldOverlay;
        this.world = world;
        this.entities = entities;
        this.inventory = inventory;
        this.equipment = equipment;
        this.profile = profile;
        this.inventoryOverlay = inventoryOverlay;
        this.pauseOverlay = pauseOverlay;
        this.worldRenderer = worldRenderer;
        this.input = input;
        this.interaction = interaction;
        this.mining = mining;
        this.placement = placement;
        this.eat = eat;
        this.dropSystem = dropSystem;
        this.dropRenderer = dropRenderer;
        this.hotbar = hotbar;
        this.vitalsHud = vitalsHud;
        this.statusHud = statusHud;
        this.equipmentSidebar = equipmentSidebar;
        this.debug = debug;
        this.gameLoop = gameLoop;
        this.hud = hud;
        this.fonts = fonts;
        this.gameSettings = gameSettings;
        this.zoomController = zoomController;
        this.renderSettings = renderSettings;
    }

    public static GameContext create(HudViewport hudViewport) {
        DawnAssets assets = new DawnAssets();
        SpriteBatch worldBatch = new SpriteBatch();
        ShapeRenderer worldOverlay = new ShapeRenderer();
        World world = World.createDefault();
        EntityManager entities = new EntityManager();
        entities.spawn(EntityId.PLAYER, Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f);
        PlayerInventory inventory = new PlayerInventory();
        EquipmentInventory equipment = new EquipmentInventory();
        PlayerProfile profile = new PlayerProfile();
        profile.bindEntity(entities.getPlayer());
        WorldRenderer worldRenderer = new WorldRenderer(worldBatch, worldOverlay, assets);
        InputController input = new InputController();
        LootTable lootTable = new LootTable();
        DropSystem dropSystem = new DropSystem();
        DropRenderer dropRenderer = new DropRenderer();
        InteractionSystem interaction = new InteractionSystem(lootTable, dropSystem);
        MiningSystem mining = new MiningSystem(interaction);
        PlacementSystem placement = new PlacementSystem(interaction);
        EatSystem eat = new EatSystem();
        SimulationSystem simulation = new SimulationSystem(world);
        GameLoop gameLoop = new GameLoop(simulation);
        DawnFonts fonts = new DawnFonts();
        HudAssets hud = new HudAssets(fonts);
        GameSettings gameSettings = new GameSettings();
        RenderSettings renderSettings = new RenderSettings();
        gameSettings.applyDisplayGamma(renderSettings);
        Hotbar hotbar = new Hotbar(hud, assets, inventory, gameSettings);
        VitalsHud vitalsHud = new VitalsHud(hud, assets, gameSettings);
        StatusHud statusHud = new StatusHud(hud, assets);
        EquipmentSidebarHud equipmentSidebar =
                new EquipmentSidebarHud(
                        fonts,
                        assets,
                        inventory,
                        equipment,
                        dropSystem,
                        entities.getPlayer(),
                        gameSettings,
                        hotbar,
                        hudViewport);
        DebugOverlay debug = new DebugOverlay(hud);
        ZoomController zoomController = new ZoomController(gameSettings);
        InventoryOverlay inventoryOverlay =
                new InventoryOverlay(
                        fonts,
                        assets,
                        inventory,
                        equipment,
                        profile,
                        dropSystem,
                        entities.getPlayer());
        PauseOverlay pauseOverlay = new PauseOverlay(assets, fonts, gameSettings, renderSettings);
        return new GameContext(
                assets,
                worldBatch,
                worldOverlay,
                world,
                entities,
                inventory,
                equipment,
                profile,
                inventoryOverlay,
                pauseOverlay,
                worldRenderer,
                input,
                interaction,
                mining,
                placement,
                eat,
                dropSystem,
                dropRenderer,
                hotbar,
                vitalsHud,
                statusHud,
                equipmentSidebar,
                debug,
                gameLoop,
                hud,
                fonts,
                gameSettings,
                zoomController,
                renderSettings);
    }

    @Override
    public void dispose() {
        worldBatch.dispose();
        worldOverlay.dispose();
        worldRenderer.dispose();
        assets.dispose();
        hud.dispose();
        fonts.dispose();
        inventoryOverlay.dispose();
        equipmentSidebar.dispose();
        pauseOverlay.dispose();
    }
}
