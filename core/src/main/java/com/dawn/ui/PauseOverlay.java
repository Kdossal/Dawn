package com.dawn.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.dawn.ui.DawnFonts;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.render.GameSettings;
import com.dawn.render.RenderColors;
import com.dawn.render.RenderSettings;

/** ESC-toggled pause screen with main menu and options sub-screen. */
public final class PauseOverlay implements Disposable {
    private final DawnAssets assets;
    private final DawnFonts fonts;
    private final TextureRegion whitePixel;
    private final GameSettings gameSettings;
    private Stage stage;
    private Table mainMenu;
    private Table optionsPanel;
    private Label zoomValueLabel;
    private TextButton uiSizeButton;
    private TextButton gammaButton;
    private final RenderSettings renderSettings;
    private boolean paused;
    private boolean showingOptions;
    private Runnable onResume = () -> {};
    private Runnable onExit = () -> {};

    public PauseOverlay(DawnAssets assets, DawnFonts fonts, GameSettings gameSettings, RenderSettings renderSettings) {
        this.assets = assets;
        this.fonts = fonts;
        this.whitePixel = assets.whitePixel;
        this.gameSettings = gameSettings;
        this.renderSettings = renderSettings;
        gameSettings.applyDisplayGamma(renderSettings);
        initStage();
        unpause();
    }

    /** Headless-friendly: state only, no Stage/GL. */
    static PauseOverlay forStateTest() {
        PauseOverlay overlay = new PauseOverlay();
        overlay.unpause();
        return overlay;
    }

    private PauseOverlay() {
        assets = null;
        fonts = null;
        whitePixel = null;
        gameSettings = null;
        renderSettings = null;
    }

    public void setCallbacks(Runnable onResume, Runnable onExit) {
        this.onResume = onResume != null ? onResume : () -> {};
        this.onExit = onExit != null ? onExit : () -> {};
    }

    public Stage stage() {
        if (whitePixel == null) {
            throw new IllegalStateException("Pause overlay stage not available in state-test mode");
        }
        return stage;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused = true;
        if (stage == null) {
            return;
        }
        showMainMenu();
        stage.getRoot().setVisible(true);
    }

    public void unpause() {
        paused = false;
        showingOptions = false;
        if (stage != null) {
            stage.getRoot().setVisible(false);
            showMainMenu();
        }
    }

    public void toggle() {
        if (paused) {
            unpause();
        } else {
            pause();
        }
    }

    public void act(float delta) {
        if (!paused || stage == null) {
            return;
        }
        stage.act(delta);
    }

    public void draw() {
        if (!paused || stage == null) {
            return;
        }
        stage.draw();
    }

    public void handleToggleKey() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            return;
        }
        if (!paused) {
            pause();
            return;
        }
        if (showingOptions) {
            showMainMenu();
            return;
        }
        requestResume();
    }

    public void onResize(int screenWidth, int screenHeight) {
        if (stage == null) {
            return;
        }
        stage.getViewport().update(screenWidth, screenHeight, true);
    }

    private void requestResume() {
        unpause();
        onResume.run();
    }

    private void showMainMenu() {
        showingOptions = false;
        if (mainMenu != null) {
            mainMenu.setVisible(true);
        }
        if (optionsPanel != null) {
            optionsPanel.setVisible(false);
        }
    }

    private void showOptions() {
        showingOptions = true;
        refreshZoomUi();
        refreshUiSizeUi();
        refreshGammaUi();
        if (mainMenu != null) {
            mainMenu.setVisible(false);
        }
        if (optionsPanel != null) {
            optionsPanel.setVisible(true);
        }
    }

    private void adjustZoom(int delta) {
        gameSettings.adjustZoomLevel(delta);
        refreshZoomUi();
    }

    private void refreshZoomUi() {
        if (zoomValueLabel == null) {
            return;
        }
        zoomValueLabel.setText(gameSettings.zoomLevel + " / " + GameSettings.MAX_ZOOM_LEVEL);
    }

    private void toggleUiSize() {
        gameSettings.cycleUiSize();
        refreshUiSizeUi();
    }

    private void refreshUiSizeUi() {
        if (uiSizeButton == null) {
            return;
        }
        uiSizeButton.setText(GameSettings.uiSizeLabel(gameSettings.uiSize));
    }

    private void cycleDisplayGamma() {
        gameSettings.cycleDisplayGammaPreset();
        gameSettings.applyDisplayGamma(renderSettings);
        refreshGammaUi();
    }

    private void refreshGammaUi() {
        if (gammaButton == null) {
            return;
        }
        gammaButton.setText(GameSettings.displayGammaLabel(gameSettings.displayGammaPreset));
    }

    private void initStage() {
        stage = new Stage(new FitViewport(Constants.HUD_WIDTH_PX, Constants.HUD_HEIGHT_PX));
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        Table dimLayer = new Table();
        dimLayer.setFillParent(true);
        dimLayer.setBackground(dimDrawable(whitePixel));
        dimLayer.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        stage.addActor(dimLayer);

        mainMenu = buildMainMenu();
        optionsPanel = buildOptionsPanel();
        optionsPanel.setVisible(false);

        Stack uiRoot = new Stack();
        uiRoot.setFillParent(true);
        uiRoot.add(mainMenu);
        uiRoot.add(optionsPanel);
        stage.addActor(uiRoot);
    }

    private Table buildMainMenu() {
        Table menu = new Table();
        menu.setFillParent(true);
        menu.center();

        Table column = new Table();
        column.add(PauseUiStyle.titleLabel("DAWN", fonts)).padBottom(PauseUiStyle.TITLE_BOTTOM_PAD).row();

        TextButton resume = PauseUiStyle.button("Resume", fonts, assets);
        resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                requestResume();
            }
        });
        column.add(resume)
                .width(PauseUiStyle.BUTTON_WIDTH)
                .height(PauseUiStyle.BUTTON_HEIGHT)
                .padBottom(PauseUiStyle.BUTTON_GAP)
                .row();

        TextButton options = PauseUiStyle.button("Options", fonts, assets);
        options.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showOptions();
            }
        });
        column.add(options)
                .width(PauseUiStyle.BUTTON_WIDTH)
                .height(PauseUiStyle.BUTTON_HEIGHT)
                .padBottom(PauseUiStyle.BUTTON_GAP)
                .row();

        TextButton exit = PauseUiStyle.button("Exit", fonts, assets);
        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onExit.run();
            }
        });
        column.add(exit).width(PauseUiStyle.BUTTON_WIDTH).height(PauseUiStyle.BUTTON_HEIGHT).row();

        menu.add(column);
        return menu;
    }

    private Table buildOptionsPanel() {
        Table panel = new Table();
        panel.setFillParent(true);
        panel.center();

        Table column = new Table();
        column.add(PauseUiStyle.sectionLabel("Options", fonts)).padBottom(32f).row();

        Table zoomRow = new Table();
        zoomRow.add(PauseUiStyle.sectionLabel("Zoom", fonts)).padRight(16f);

        TextButton minus = PauseUiStyle.smallButton("-", fonts, assets);
        minus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                adjustZoom(-1);
            }
        });
        zoomRow.add(minus).size(44f, 44f).padRight(8f);

        zoomValueLabel = PauseUiStyle.sectionLabel("1 / 10", fonts);
        zoomValueLabel.setAlignment(Align.center);
        zoomRow.add(zoomValueLabel).width(100f).padRight(8f);

        TextButton plus = PauseUiStyle.smallButton("+", fonts, assets);
        plus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                adjustZoom(1);
            }
        });
        zoomRow.add(plus).size(44f, 44f);
        column.add(zoomRow).padBottom(36f).row();

        Table uiSizeRow = new Table();
        uiSizeRow.add(PauseUiStyle.sectionLabel("UI Size", fonts)).padRight(16f);
        uiSizeButton = PauseUiStyle.smallButton(GameSettings.uiSizeLabel(gameSettings.uiSize), fonts, assets);
        uiSizeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleUiSize();
            }
        });
        uiSizeRow.add(uiSizeButton).width(160f).height(44f);
        column.add(uiSizeRow).padBottom(36f).row();

        Table gammaRow = new Table();
        gammaRow.add(PauseUiStyle.sectionLabel("Gamma", fonts)).padRight(16f);
        gammaButton =
                PauseUiStyle.smallButton(
                        GameSettings.displayGammaLabel(gameSettings.displayGammaPreset), fonts, assets);
        gammaButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cycleDisplayGamma();
            }
        });
        gammaRow.add(gammaButton).width(160f).height(44f);
        column.add(gammaRow).padBottom(36f).row();

        TextButton back = PauseUiStyle.button("Back", fonts, assets);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showMainMenu();
            }
        });
        column.add(back).width(PauseUiStyle.BUTTON_WIDTH).height(PauseUiStyle.BUTTON_HEIGHT).row();

        panel.add(column);
        refreshZoomUi();
        return panel;
    }

    private static Drawable dimDrawable(TextureRegion white) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(white) {
            @Override
            public void draw(Batch batch, float x, float y, float width, float height) {
                Color old = batch.getColor();
                batch.setColor(
                        RenderColors.PAUSE_DIM_COLOR.r,
                        RenderColors.PAUSE_DIM_COLOR.g,
                        RenderColors.PAUSE_DIM_COLOR.b,
                        RenderColors.PAUSE_DIM_ALPHA);
                super.draw(batch, x, y, width, height);
                batch.setColor(old);
            }
        };
        return drawable;
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
    }
}
