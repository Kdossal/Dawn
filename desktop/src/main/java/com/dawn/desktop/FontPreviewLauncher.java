package com.dawn.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.dawn.config.Constants;
import com.dawn.ui.FontPreviewScreen;
import com.badlogic.gdx.Game;

public final class FontPreviewLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Dawn — Font Preview");
        config.setWindowedMode(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(
                new Game() {
                    @Override
                    public void create() {
                        setScreen(new FontPreviewScreen());
                    }
                },
                config);
    }

    private FontPreviewLauncher() {}
}
