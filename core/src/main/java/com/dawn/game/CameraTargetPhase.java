package com.dawn.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.dawn.config.Constants;
import com.dawn.entity.Entity;
import com.dawn.render.GameViewport;

/** Handles camera synchronization and mouse/target updates. */
final class CameraTargetPhase {
    void syncCamera(GameContext ctx, OrthographicCamera worldCamera) {
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

    void tick(
            GameContext ctx, GameViewport gameViewport, OrthographicCamera worldCamera, FrameState frame) {
        syncCamera(ctx, worldCamera);
        gameViewport.apply(worldCamera);
        frame.mouseWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        gameViewport.unproject(frame.mouseWorld);
        Entity player = ctx.entities.getPlayer();
        frame.target =
                ctx.input.updateTarget(
                        ctx.world,
                        player,
                        frame.mouseWorld,
                        ctx.equipmentSidebar.interactionHeld(ctx.hotbar.getHeld()));
    }
}
