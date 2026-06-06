package com.dawn.world.render.highlight;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.assets.DawnAssets;
import com.dawn.render.AlphaMaskShader;
import com.dawn.world.World;
import com.dawn.world.structure.StructureKind;
import com.dawn.world.structure.StructureSprites;
import java.nio.IntBuffer;
import java.util.List;

/** One even highlight over combined structure art (alpha mask, no stacked rects). */
public final class StructureMaskHighlightRenderer implements Disposable {
    private final OrthographicCamera fboCamera = new OrthographicCamera();
    private final AlphaMaskShader maskShader = new AlphaMaskShader();
    private final IntBuffer savedViewport = BufferUtils.newIntBuffer(4);
    private FrameBuffer buffer;
    private TextureRegion bufferRegion;

    public void render(
            SpriteBatch batch,
            DawnAssets assets,
            World world,
            int anchorX,
            int anchorY,
            StructureKind kind,
            Color tint) {
        List<StructureSprites.Sprite> sprites =
                StructureSprites.collectPlaced(world, assets, kind, anchorX, anchorY);
        renderSprites(batch, sprites, tint);
    }

    /** Masked tint for blueprint or placed structure sprites. */
    public void renderSprites(SpriteBatch batch, List<StructureSprites.Sprite> sprites, Color tint) {
        if (sprites.isEmpty()) {
            return;
        }

        float minPx = Float.MAX_VALUE;
        float minPy = Float.MAX_VALUE;
        float maxPx = -Float.MAX_VALUE;
        float maxPy = -Float.MAX_VALUE;
        for (StructureSprites.Sprite s : sprites) {
            minPx = Math.min(minPx, s.px());
            minPy = Math.min(minPy, s.py());
            maxPx = Math.max(maxPx, s.px() + s.w());
            maxPy = Math.max(maxPy, s.py() + s.h());
        }

        int bufW = Math.max(1, (int) Math.ceil(maxPx - minPx));
        int bufH = Math.max(1, (int) Math.ceil(maxPy - minPy));
        ensureBuffer(bufW, bufH);

        Matrix4 savedProj = batch.getProjectionMatrix().cpy();
        ShaderProgram savedShader = batch.getShader();
        batch.flush();

        savedViewport.clear();
        Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, savedViewport);
        buffer.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        fboCamera.setToOrtho(false, bufW, bufH);
        fboCamera.update();
        batch.setProjectionMatrix(fboCamera.combined);
        batch.setShader(maskShader);
        batch.setColor(Color.WHITE);
        for (StructureSprites.Sprite s : sprites) {
            batch.draw(s.region(), s.px() - minPx, s.py() - minPy, s.w(), s.h());
        }
        batch.flush();
        buffer.end(savedViewport.get(0), savedViewport.get(1), savedViewport.get(2), savedViewport.get(3));

        batch.setShader(savedShader);
        batch.setProjectionMatrix(savedProj);
        batch.setColor(tint.r, tint.g, tint.b, tint.a);
        bufferRegion.flip(false, true);
        batch.draw(bufferRegion, minPx, minPy, bufW, bufH);
        bufferRegion.flip(false, true);
        batch.setColor(Color.WHITE);
    }

    private void ensureBuffer(int width, int height) {
        if (buffer != null && buffer.getWidth() == width && buffer.getHeight() == height) {
            return;
        }
        if (buffer != null) {
            buffer.dispose();
        }
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        bufferRegion = new TextureRegion(buffer.getColorBufferTexture());
        bufferRegion.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    @Override
    public void dispose() {
        maskShader.dispose();
        if (buffer != null) {
            buffer.dispose();
            buffer = null;
            bufferRegion = null;
        }
    }
}
