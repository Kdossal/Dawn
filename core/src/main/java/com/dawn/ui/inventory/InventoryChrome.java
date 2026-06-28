package com.dawn.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.dawn.assets.DawnAssets;

/** Full-screen inventory chrome background only (Phase 1). */
public final class InventoryChrome extends Group {
    public InventoryChrome(DawnAssets assets) {
        setSize(InventoryOverlayDesign.chromeW(), InventoryOverlayDesign.chromeH());

        Image chromeBg = new Image(InventoryUiStyle.fixedDrawable(assets.uiInventory.chromeBg));
        chromeBg.setSize(InventoryOverlayDesign.chromeW(), InventoryOverlayDesign.chromeH());
        chromeBg.setPosition(0f, 0f);
        addActor(chromeBg);
    }
}
