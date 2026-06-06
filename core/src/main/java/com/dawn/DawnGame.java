package com.dawn;

import com.badlogic.gdx.Game;
import com.dawn.game.GameScreen;

public class DawnGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
