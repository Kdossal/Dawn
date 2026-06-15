package com.dawn.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntSet;
import com.dawn.config.Constants;
import com.dawn.gameplay.TargetResolver;
import com.dawn.gameplay.TargetResolver.TargetCell;
import com.dawn.entity.Entity;
import com.dawn.item.ItemStack;
import com.dawn.world.World;

/** Keyboard movement tracking and world targeting. */
public class InputController extends InputAdapter {
    private final IntSet held = new IntSet();
    private final DoubleTapRunTracker runTracker = new DoubleTapRunTracker();
    private TargetCell targetCell;
    private float scrollYAccum;

    @Override
    public boolean keyDown(int keycode) {
        held.add(keycode);
        runTracker.onKeyDown(keycode, System.nanoTime() / 1_000_000_000d);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        held.remove(keycode);
        runTracker.onKeyUp(keycode, isAnyMovementHeld());
        return false;
    }

    public boolean isHeld(int keycode) {
        return held.contains(keycode) || Gdx.input.isKeyPressed(keycode);
    }

    public float getMoveX() {
        float x = 0f;
        if (isHeld(Input.Keys.A) || isHeld(Input.Keys.LEFT)) {
            x -= 1f;
        }
        if (isHeld(Input.Keys.D) || isHeld(Input.Keys.RIGHT)) {
            x += 1f;
        }
        return x;
    }

    public float getMoveY() {
        float y = 0f;
        if (isHeld(Input.Keys.S) || isHeld(Input.Keys.DOWN)) {
            y -= 1f;
        }
        if (isHeld(Input.Keys.W) || isHeld(Input.Keys.UP)) {
            y += 1f;
        }
        return y;
    }

    /** True after a movement-key double-tap while at least one movement key is still held. */
    public boolean isRunning() {
        return runTracker.isRunning() && (getMoveX() != 0f || getMoveY() != 0f);
    }

    /** Sprint active and entity has energy to keep running this frame. */
    public boolean isRunningWithEnergy(float currentEnergy) {
        return currentEnergy > 0f && isRunning();
    }

    public void cancelRun() {
        runTracker.cancelRun();
    }

    private boolean isAnyMovementHeld() {
        for (int key : MovementKeys.all()) {
            if (isHeld(key)) {
                return true;
            }
        }
        return false;
    }

    public TargetCell updateTarget(World world, Entity player, Vector3 mouseWorldPx, ItemStack held) {
        int mouseCellX = (int) Math.floor(mouseWorldPx.x / Constants.CELL_SIZE_PX);
        int mouseCellY = (int) Math.floor(mouseWorldPx.y / Constants.CELL_SIZE_PX);
        targetCell =
                TargetResolver.resolve(
                        world, player.def(), player.getX(), player.getY(), mouseCellX, mouseCellY, held);
        return targetCell;
    }

    public TargetCell getTargetCell() {
        return targetCell;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        scrollYAccum += amountY;
        return false;
    }

    /** Wheel delta from scroll events only (not mouse movement). */
    public float consumeScrollY() {
        float delta = scrollYAccum;
        scrollYAccum = 0f;
        return delta;
    }

    public boolean miningHeld() {
        return Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    }

    public boolean placeHeld() {
        return Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
    }

    public boolean placeJustPressed() {
        return Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
    }

    public boolean dropPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.Q);
    }

    public boolean dropFullStackPressed() {
        return dropPressed() && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));
    }
}
