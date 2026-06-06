package com.dawn.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.assets.DawnAssets;

/** Applies a custom hardware cursor from {@code assets/ui/common/cursor.png}. */
public final class GameCursor implements Disposable {
    private static final int HOTSPOT_X = 2;
    private static final int HOTSPOT_Y = 2;

    private Cursor cursor;

    public void apply(DawnAssets assets) {
        dispose();
        Pixmap pixmap = new Pixmap(Gdx.files.internal("ui/common/cursor.png"));
        cursor = Gdx.graphics.newCursor(pixmap, HOTSPOT_X, HOTSPOT_Y);
        pixmap.dispose();
        Gdx.graphics.setCursor(cursor);
    }

    @Override
    public void dispose() {
        if (cursor != null) {
            cursor.dispose();
            cursor = null;
        }
    }
}
