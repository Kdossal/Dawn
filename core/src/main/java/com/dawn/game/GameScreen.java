package com.dawn.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.dawn.config.Constants;
import com.dawn.entity.Entity;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.gameplay.drops.WorldDrop;
import com.dawn.input.GameCursor;
import com.dawn.render.GameViewport;
import com.dawn.render.HudViewport;
import com.dawn.ui.DebugOverlay;

public class GameScreen extends ScreenAdapter {
    private OrthographicCamera worldCamera;
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final GameViewport gameViewport = new GameViewport();
    private final HudViewport hudViewport = new HudViewport();
    private final ScreenRenderer screenRenderer = new ScreenRenderer();
    private final Vector3 mouseWorld = new Vector3();

    private GameContext ctx;
    private final GameCursor gameCursor = new GameCursor();
    private InputAdapter hotbarInput;
    private float lastMoveX;
    private float lastMoveY;
    private TargetCell target;

    @Override
    public void show() {
        ctx = GameContext.create();

        hotbarInput =
                new InputAdapter() {
                    @Override
                    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                        return ctx.hotbar.handleClick(screenX, screenY);
                    }
                };
        refreshInputProcessors();

        worldCamera = new OrthographicCamera();
        applyHudCameraSize();
        ctx.zoomController.applyTo(worldCamera, gameViewport);
        updateCamera();
        ctx.inventoryOverlay.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ctx.pauseOverlay.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ctx.pauseOverlay.setCallbacks(this::resumeFromPause, Gdx.app::exit);
        gameCursor.apply(ctx.assets);
    }

    @Override
    public void render(float delta) {
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        gameViewport.update(screenW, screenH);
        hudViewport.update(screenW, screenH);

        Gdx.gl.glClearColor(0.12f, 0.14f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean wasPaused = ctx.pauseOverlay.isPaused();
        ctx.pauseOverlay.handleToggleKey();
        if (!wasPaused && ctx.pauseOverlay.isPaused()) {
            ctx.inventoryOverlay.close();
            ctx.mining.reset();
            ctx.interactionPresentation.clear();
            refreshInputProcessors();
        } else if (wasPaused && !ctx.pauseOverlay.isPaused()) {
            refreshInputProcessors();
        }

        if (!ctx.pauseOverlay.isPaused()) {
            if (Gdx.input.isKeyJustPressed(DebugOverlay.TOGGLE_KEY)) {
                ctx.debug.toggle();
            }

            boolean wasOpen = ctx.inventoryOverlay.isOpen();
            ctx.inventoryOverlay.handleToggleKey();
            if (wasOpen != ctx.inventoryOverlay.isOpen()) {
                refreshInputProcessors();
            }

            int prevActiveRow = ctx.inventory.getActiveRow();
            ctx.hotbar.update();
            if (ctx.inventoryOverlay.isOpen() && prevActiveRow != ctx.inventory.getActiveRow()) {
                ctx.inventoryOverlay.refreshAll();
            }

            float scrollY = ctx.input.consumeScrollY();
            if (scrollY != 0f && !ctx.inventoryOverlay.isOpen()) {
                ctx.hotbar.applyScroll(scrollY);
            }
        }

        ctx.zoomController.update(delta);
        ctx.zoomController.applyTo(worldCamera, gameViewport);

        if (ctx.pauseOverlay.isPaused()) {
            updatePaused(delta);
            updateCamera();
        } else {
            update(delta);
        }

        WorldDrop hovered = null;
        if (!ctx.pauseOverlay.isPaused()) {
            float mouseCellX = mouseWorld.x / Constants.CELL_SIZE_PX;
            float mouseCellY = mouseWorld.y / Constants.CELL_SIZE_PX;
            hovered = ctx.dropRenderer.findHovered(ctx.dropSystem.getDrops(), mouseCellX, mouseCellY);
        }

        screenRenderer.render(
                ctx,
                gameViewport,
                hudViewport,
                worldCamera,
                hudCamera,
                target,
                hovered,
                mouseWorld,
                ctx.input,
                ctx.entities.getPlayer(),
                lastMoveX,
                lastMoveY,
                delta,
                ctx.interaction.getLastMessage(),
                ctx.gameLoop.getSimulation().getCurrentTick());
    }

    private void resumeFromPause() {
        refreshInputProcessors();
    }

    private void updatePaused(float delta) {
        ctx.pauseOverlay.act(delta);
    }

    private void update(float delta) {
        Entity player = ctx.entities.getPlayer();
        lastMoveX = ctx.input.getMoveX();
        lastMoveY = ctx.input.getMoveY();
        if (lastMoveX != 0f || lastMoveY != 0f) {
            float len = (float) Math.sqrt(lastMoveX * lastMoveX + lastMoveY * lastMoveY);
            lastMoveX /= len;
            lastMoveY /= len;
            float speed = player.getMoveSpeedCellsPerSec(ctx.input.isRunning()) * delta;
            player.move(lastMoveX * speed, lastMoveY * speed, ctx.world);
        }

        updateCamera();
        unprojectMouseWorld();
        target = ctx.input.updateTarget(ctx.world, player, mouseWorld, ctx.hotbar.getHeld());

        if (ctx.inventoryOverlay.isOpen()) {
            ctx.mining.reset();
            ctx.interactionPresentation.clear();
            ctx.inventoryOverlay.act(delta);
            updateSimulationRegions();
            ctx.gameLoop.update(delta);
            ctx.dropSystem.update(delta);
            ctx.dropSystem.tryPickupAll(player, ctx.inventory);
            return;
        }

        boolean overHotbar =
                ctx.hotbar.hitTest(Gdx.input.getX(), Gdx.input.getY()) != null;
        if (!overHotbar) {
            ctx.mining.update(
                    ctx.world,
                    player,
                    target,
                    ctx.hotbar.getHeld(),
                    ctx.input.miningHeld(),
                    delta);

            ctx.placement.update(
                    ctx.world,
                    player,
                    ctx.inventory,
                    target,
                    ctx.hotbar.getHeld(),
                    ctx.input.placeHeld(),
                    delta);
        } else {
            ctx.mining.reset();
        }

        if (ctx.input.dropFullStackPressed()) {
            ctx.dropSystem.dropFromEntity(player, ctx.inventory, true);
        } else if (ctx.input.dropPressed()) {
            ctx.dropSystem.dropFromEntity(player, ctx.inventory, false);
        }

        ctx.dropSystem.update(delta);
        ctx.dropSystem.tryPickupAll(player, ctx.inventory);

        ctx.interactionPresentation.update(
                ctx.world,
                player,
                ctx.hotbar.getHeld(),
                target,
                !ctx.input.placeHeld());

        updateSimulationRegions();
        ctx.gameLoop.update(delta);
    }

    private void unprojectMouseWorld() {
        gameViewport.apply(worldCamera);
        mouseWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        gameViewport.unproject(mouseWorld);
    }

    private void updateSimulationRegions() {
        Entity player = ctx.entities.getPlayer();
        float halfW = ctx.zoomController.viewWidthPx() / 2f;
        float halfH = ctx.zoomController.viewHeightPx() / 2f;
        ctx.gameLoop
                .getSimulation()
                .updateActiveRegions(
                        worldCamera.position.x - halfW,
                        worldCamera.position.y - halfH,
                        worldCamera.position.x + halfW,
                        worldCamera.position.y + halfH,
                        player.getX(),
                        player.getY());
    }

    private void updateCamera() {
        Entity player = ctx.entities.getPlayer();
        float playerPxX = player.getX() * Constants.CELL_SIZE_PX;
        float playerPxY = player.getY() * Constants.CELL_SIZE_PX;
        worldCamera.position.x = playerPxX;
        worldCamera.position.y = playerPxY;

        float halfW = ctx.zoomController.viewWidthPx() / 2f;
        float halfH = ctx.zoomController.viewHeightPx() / 2f;
        float minX = halfW;
        float maxX = Constants.MAP_WIDTH_PX - halfW;
        float minY = halfH;
        float maxY = Constants.MAP_HEIGHT_PX - halfH;

        if (maxX < minX) {
            worldCamera.position.x = Constants.MAP_WIDTH_PX / 2f;
        } else {
            worldCamera.position.x = Math.max(minX, Math.min(maxX, worldCamera.position.x));
        }

        if (maxY < minY) {
            worldCamera.position.y = Constants.MAP_HEIGHT_PX / 2f;
        } else {
            worldCamera.position.y = Math.max(minY, Math.min(maxY, worldCamera.position.y));
        }
    }

    private void applyHudCameraSize() {
        hudCamera.setToOrtho(false, Constants.HUD_WIDTH_PX, Constants.HUD_HEIGHT_PX);
    }

    private void refreshInputProcessors() {
        if (ctx.pauseOverlay.isPaused()) {
            Gdx.input.setInputProcessor(ctx.pauseOverlay.stage());
        } else if (ctx.inventoryOverlay.isOpen()) {
            Gdx.input.setInputProcessor(
                    new InputMultiplexer(ctx.inventoryOverlay.stage(), ctx.input));
        } else {
            Gdx.input.setInputProcessor(new InputMultiplexer(hotbarInput, ctx.input));
        }
    }

    @Override
    public void resize(int width, int height) {
        applyHudCameraSize();
        gameViewport.update(width, height);
        hudViewport.update(width, height);
        if (ctx != null) {
            ctx.zoomController.applyTo(worldCamera, gameViewport);
            ctx.inventoryOverlay.onResize(width, height);
            ctx.pauseOverlay.onResize(width, height);
            updateCamera();
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow);
    }

    @Override
    public void dispose() {
        gameCursor.dispose();
        if (ctx != null) {
            ctx.dispose();
        }
    }
}
