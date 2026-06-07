package com.dawn.world.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.dawn.entity.sprite.EntitySpriteFrame;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.assets.DawnAssets;
import com.dawn.config.Constants;
import com.dawn.entity.EntityBounds;
import com.dawn.entity.EntityCollision;
import com.dawn.gameplay.InteractionHighlight;
import com.dawn.gameplay.drops.WorldDrop;
import com.dawn.gameplay.placement.PlacementPreview;
import com.dawn.render.RenderColors;
import com.dawn.world.World;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.autotile.AutotileCell;
import com.dawn.world.block.autotile.AutotileFamily;
import com.dawn.world.block.autotile.AutotileRegistry;
import com.dawn.world.block.autotile.AutotileResolver;
import com.dawn.world.block.visual.BlockSpriteDraw;
import com.dawn.world.block.visual.BlockVisualDef;
import com.dawn.world.block.visual.BlockVisualRegistry;
import com.dawn.world.render.highlight.InteractionHighlightRenderer;
import com.dawn.world.render.highlight.PlacementGhostRenderer;
import com.dawn.world.render.highlight.StructureMaskHighlightRenderer;
import java.util.List;

public class WorldRenderer implements Disposable {
    private final SpriteBatch batch;
    private final ShapeRenderer overlay;
    private final DawnAssets assets;
    private final StructureMaskHighlightRenderer structureMaskRenderer = new StructureMaskHighlightRenderer();
    private final InteractionHighlightRenderer highlightRenderer;
    private final PlacementGhostRenderer placementGhostRenderer;

    public WorldRenderer(SpriteBatch batch, ShapeRenderer overlay, DawnAssets assets) {
        this.batch = batch;
        this.overlay = overlay;
        this.assets = assets;
        this.highlightRenderer = new InteractionHighlightRenderer(structureMaskRenderer);
        this.placementGhostRenderer = new PlacementGhostRenderer(structureMaskRenderer);
    }

    @Override
    public void dispose() {
        structureMaskRenderer.dispose();
    }

    public void renderPlacementGhosts(
            List<PlacementPreview> previews, float pixelAlignOffsetX, float pixelAlignOffsetY) {
        placementGhostRenderer.render(batch, assets, previews, pixelAlignOffsetX, pixelAlignOffsetY);
    }

    public void renderTerrain(
            World world, int minX, int maxX, int minY, int maxY, float pixelAlignOffsetX, float pixelAlignOffsetY) {
        applyPixelAlign(batch, pixelAlignOffsetX, pixelAlignOffsetY);
        batch.setColor(Color.WHITE);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (world.inBounds(x, y)) {
                    drawTerrainCell(world, x, y);
                }
            }
        }
        clearPixelAlign(batch, pixelAlignOffsetX, pixelAlignOffsetY);
    }

    public void renderSortedWorld(
            World world,
            int minX,
            int maxX,
            int minY,
            int maxY,
            float playerFeetX,
            float playerFeetY,
            EntitySpriteFrame playerSprite,
            EntityBounds playerMoveBox,
            boolean occlusionFadeEnabled,
            List<WorldDrop> drops,
            float pixelAlignOffsetX,
            float pixelAlignOffsetY) {
        List<WorldDrawable> drawables = WorldDrawCollector.collect(
                world, minX, maxX, minY, maxY, playerFeetX, playerFeetY, playerSprite, drops);
        WorldDrawableSort.sort(drawables);
        DrawContext context =
                DrawContext.create(
                        world,
                        drawables,
                        playerMoveBox,
                        playerFeetX,
                        playerFeetY,
                        playerSprite,
                        assets,
                        occlusionFadeEnabled,
                        pixelAlignOffsetX,
                        pixelAlignOffsetY);
        batch.setColor(Color.WHITE);
        for (WorldDrawable drawable : drawables) {
            drawable.draw(batch, assets, context);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawTerrainCell(World world, int x, int y) {
        BlockId ground = world.getGround(x, y);
        AutotileFamily groundAutotile = AutotileRegistry.familyFor(ground);
        if (groundAutotile != null) {
            drawAutotile(world, groundAutotile, x, y);
        } else {
            drawBlockId(ground, x, y);
        }

        BlockId floor = world.getFloor(x, y);
        if (floor != BlockId.AIR) {
            AutotileFamily floorAutotile = AutotileRegistry.familyFor(floor);
            if (floorAutotile != null) {
                drawAutotile(world, floorAutotile, x, y);
            } else {
                drawBlockId(floor, x, y);
            }
        }
    }

    private void drawAutotile(World world, AutotileFamily family, int cellX, int cellY) {
        BlockVisualDef visual = BlockVisualRegistry.get(family.blockId());
        if (visual == null) {
            return;
        }
        AutotileCell cell = AutotileResolver.resolve(world, cellX, cellY, family);
        TextureRegion region = assets.autotileRegion(family.texture(), cell);
        BlockSpriteDraw.drawRegion(batch, assets, region, visual, cellX, cellY);
    }

    private void drawBlockId(BlockId id, int cellX, int cellY) {
        BlockVisualDef visual = BlockVisualRegistry.get(id);
        if (visual != null) {
            BlockSpriteDraw.drawBlock(batch, assets, visual, cellX, cellY);
        }
    }

    public void renderInteractionHighlights(
            World world,
            List<InteractionHighlight.Highlight> highlights,
            float pixelAlignOffsetX,
            float pixelAlignOffsetY) {
        highlightRenderer.render(batch, assets, world, highlights, pixelAlignOffsetX, pixelAlignOffsetY);
    }

    private static void applyPixelAlign(SpriteBatch batch, float offsetX, float offsetY) {
        if (offsetX != 0f || offsetY != 0f) {
            Matrix4 transform = batch.getTransformMatrix();
            transform.translate(offsetX, offsetY, 0f);
            batch.setTransformMatrix(transform);
        }
    }

    private static void clearPixelAlign(SpriteBatch batch, float offsetX, float offsetY) {
        if (offsetX != 0f || offsetY != 0f) {
            Matrix4 transform = batch.getTransformMatrix();
            transform.translate(-offsetX, -offsetY, 0f);
            batch.setTransformMatrix(transform);
        }
    }

    public void renderEntityCollisionDebug(World world, EntityBounds bounds, Color moveColor, Color spriteColor) {
        overlay.begin(ShapeRenderer.ShapeType.Filled);
        renderOverlappedCells(world, bounds);
        drawRectFilled(bounds.moveLeft, bounds.moveBottom, bounds.moveRight, bounds.moveTop, moveColor, 0.25f);
        overlay.end();

        overlay.begin(ShapeRenderer.ShapeType.Line);
        drawRectOutline(bounds.moveLeft, bounds.moveBottom, bounds.moveRight, bounds.moveTop, moveColor, 1f);
        drawRectOutline(bounds.spriteLeft, bounds.spriteBottom, bounds.spriteRight, bounds.spriteTop, spriteColor, 1f);
        overlay.end();
    }

    private void renderOverlappedCells(World world, EntityBounds bounds) {
        int minCellX = EntityCollision.cellMin(bounds.moveLeft);
        int maxCellX = EntityCollision.cellMax(bounds.moveRight);
        int minCellY = EntityCollision.cellMin(bounds.moveBottom);
        int maxCellY = EntityCollision.cellMax(bounds.moveTop);

        for (int cy = minCellY; cy <= maxCellY; cy++) {
            for (int cx = minCellX; cx <= maxCellX; cx++) {
                if (!EntityCollision.overlapsCell(bounds, cx, cy)) {
                    continue;
                }
                float px = cx * Constants.CELL_SIZE_PX;
                float py = cy * Constants.CELL_SIZE_PX;
                if (world.isSolidForMovement(cx, cy)) {
                    overlay.setColor(1f, 0.15f, 0.15f, 0.45f);
                } else {
                    overlay.setColor(0.2f, 0.85f, 0.35f, 0.12f);
                }
                overlay.rect(px, py, Constants.CELL_SIZE_PX, Constants.CELL_SIZE_PX);
            }
        }
    }

    private void drawRectFilled(float left, float bottom, float right, float top, Color color, float alpha) {
        float px = left * Constants.CELL_SIZE_PX;
        float py = bottom * Constants.CELL_SIZE_PX;
        float w = (right - left) * Constants.CELL_SIZE_PX;
        float h = (top - bottom) * Constants.CELL_SIZE_PX;
        overlay.setColor(color.r, color.g, color.b, alpha);
        overlay.rect(px, py, w, h);
    }

    private void drawRectOutline(float left, float bottom, float right, float top, Color color, float alpha) {
        float px = left * Constants.CELL_SIZE_PX;
        float py = bottom * Constants.CELL_SIZE_PX;
        float w = (right - left) * Constants.CELL_SIZE_PX;
        float h = (top - bottom) * Constants.CELL_SIZE_PX;
        overlay.setColor(color.r, color.g, color.b, alpha);
        overlay.rect(px, py, w, h);
    }

    public void renderReachRing(float centerXCells, float centerYCells, float reachRadiusCells) {
        float cx = centerXCells * Constants.CELL_SIZE_PX;
        float cy = centerYCells * Constants.CELL_SIZE_PX;
        float radius = reachRadiusCells * Constants.CELL_SIZE_PX;

        overlay.begin(ShapeRenderer.ShapeType.Line);
        overlay.setColor(RenderColors.REACH_RING);
        overlay.circle(cx, cy, radius);
        overlay.end();
    }

    public static int[] visibleCellBounds(
            float cameraX, float cameraY, float viewWidthPx, float viewHeightPx, int mapWidth, int mapHeight) {
        float halfW = viewWidthPx / 2f;
        float halfH = viewHeightPx / 2f;
        int minX = Math.max(0, (int) Math.floor((cameraX - halfW) / Constants.CELL_SIZE_PX) - 1);
        int maxX = Math.min(mapWidth - 1, (int) Math.ceil((cameraX + halfW) / Constants.CELL_SIZE_PX) + 1);
        int minY = Math.max(0, (int) Math.floor((cameraY - halfH) / Constants.CELL_SIZE_PX) - 1);
        int maxY = Math.min(mapHeight - 1, (int) Math.ceil((cameraY + halfH) / Constants.CELL_SIZE_PX) + 1);

        int[] pad = BlockVisualRegistry.maxCullPaddingCells();
        minX = Math.max(0, minX - pad[0]);
        maxX = Math.min(mapWidth - 1, maxX + pad[1]);
        minY = Math.max(0, minY - pad[2]);
        maxY = Math.min(mapHeight - 1, maxY + pad[3]);
        return new int[] {minX, maxX, minY, maxY};
    }
}
