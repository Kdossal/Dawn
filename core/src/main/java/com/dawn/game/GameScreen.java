package com.dawn.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.dawn.config.DayNightConfig;
import com.dawn.config.GameConfig;
import com.dawn.config.Constants;
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
    private final FrameState frame = new FrameState();
    private final UiModePhase uiModePhase = new UiModePhase();
    private final PlayerAndInteractionPhase playerAndInteractionPhase = new PlayerAndInteractionPhase();
    private final SimulationLightingPhase simulationLightingPhase = new SimulationLightingPhase();
    private final CameraTargetPhase cameraTargetPhase = new CameraTargetPhase();

    private GameContext ctx;
    private final GameCursor gameCursor = new GameCursor();
    private InputAdapter hotbarInput;
    private final Vector2 hudPointer = new Vector2();

    @Override
    public void show() {
        ctx = GameContext.create(hudViewport);

        hotbarInput =
                new InputAdapter() {
                    @Override
                    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                        if (ctx.equipmentSidebar.isDragActive()
                                || ctx.equipmentSidebar.isOpen()
                                || ctx.equipmentSidebar.isAnimating()) {
                            return false;
                        }
                        hudViewport.unproject(screenX, screenY, hudPointer);
                        return ctx.hotbar.handleClick(hudPointer.x, hudPointer.y);
                    }
                };
        uiModePhase.applyInputProcessor(ctx, hotbarInput);

        worldCamera = new OrthographicCamera();
        applyHudCameraSize();
        ctx.zoomController.applyTo(worldCamera, gameViewport);
        cameraTargetPhase.syncCamera(ctx, worldCamera);
        ctx.inventoryOverlay.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ctx.equipmentSidebar.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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

        uiModePhase.processModeToggles(ctx, frame, hotbarInput);

        if (!frame.paused) {
            if (Gdx.input.isKeyJustPressed(DebugOverlay.TOGGLE_KEY)) {
                ctx.debug.cycleMode();
            }

            if (ctx.debug.getMode().showsHudDebug()) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
                    ctx.world.clock().nudge(-0.01f);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
                    ctx.world.clock().nudge(0.01f);
                }
            }

            uiModePhase.processHotbarAndScroll(ctx);
        }

        ctx.zoomController.update(delta);
        ctx.zoomController.applyTo(worldCamera, gameViewport);

        if (frame.paused) {
            updatePaused(delta);
            cameraTargetPhase.syncCamera(ctx, worldCamera);
        } else {
            update(delta);
        }

        screenRenderer.render(
                ctx,
                gameViewport,
                hudViewport,
                worldCamera,
                hudCamera,
                frame.target,
                frame.mouseWorld,
                ctx.input,
                ctx.entities.getPlayer(),
                frame.lastMoveX,
                frame.lastMoveY,
                delta,
                ctx.interaction.getLastMessage(),
                ctx.gameLoop.getSimulation().getCurrentTick());
    }

    private void resumeFromPause() {
        uiModePhase.applyInputProcessor(ctx, hotbarInput);
    }

    private void updatePaused(float delta) {
        ctx.pauseOverlay.act(delta);
    }

    private void update(float delta) {
        ctx.world.clock().advance(delta, DayNightConfig.from(GameConfig.get()));
        ctx.equipmentSidebar.act(delta);
        playerAndInteractionPhase.tickPlayer(ctx, frame, delta);
        cameraTargetPhase.tick(ctx, gameViewport, worldCamera, frame);
        playerAndInteractionPhase.tickInteraction(ctx, frame, delta);
        simulationLightingPhase.tick(ctx, worldCamera, delta);
    }

    private void applyHudCameraSize() {
        hudCamera.setToOrtho(false, Constants.HUD_WIDTH_PX, Constants.HUD_HEIGHT_PX);
    }

    @Override
    public void resize(int width, int height) {
        applyHudCameraSize();
        gameViewport.update(width, height);
        hudViewport.update(width, height);
        if (ctx != null) {
            ctx.zoomController.applyTo(worldCamera, gameViewport);
            ctx.inventoryOverlay.onResize(width, height);
            ctx.equipmentSidebar.onResize(width, height);
            ctx.pauseOverlay.onResize(width, height);
            cameraTargetPhase.syncCamera(ctx, worldCamera);
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
