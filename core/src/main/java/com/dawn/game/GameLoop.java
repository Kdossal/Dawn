package com.dawn.game;

import com.dawn.config.GameConfig;
import com.dawn.gameplay.sim.SimulationSystem;

public class GameLoop {
    private final SimulationSystem simulation;
    private float accumulator;
    private float tickInterval;

    public GameLoop(SimulationSystem simulation) {
        this.simulation = simulation;
        resetTickInterval();
    }

    public void resetTickInterval() {
        tickInterval = 1f / GameConfig.get().simTickHz;
    }

    public void update(float delta) {
        accumulator += delta;
        while (accumulator >= tickInterval) {
            accumulator -= tickInterval;
            simulation.tick();
        }
    }

    public SimulationSystem getSimulation() {
        return simulation;
    }
}
