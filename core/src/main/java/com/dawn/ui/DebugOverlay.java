package com.dawn.ui;

import com.badlogic.gdx.Input;
import com.dawn.config.Constants;
import com.dawn.entity.AttributeId;
import com.dawn.entity.CharacterSheet;
import com.dawn.input.InputController;
import com.badlogic.gdx.utils.Disposable;
import com.dawn.inventory.EquipmentInventory;
import com.dawn.inventory.PlayerInventory;
import com.dawn.inventory.PlayerProfile;
import com.dawn.entity.Entity;
import com.dawn.gameplay.BreakTarget;
import com.dawn.config.DayNightConfig;
import com.dawn.config.GameConfig;
import com.dawn.render.AmbientLighting;
import com.dawn.ui.DawnTypography.TextContext;
import com.dawn.ui.DawnTypography.TextTier;
import com.dawn.world.World;
import com.dawn.world.WorldClock;
import com.dawn.world.block.Layer;
import com.dawn.world.light.LightMap;

public class DebugOverlay implements Disposable {
    public static final int TOGGLE_KEY = Input.Keys.F3;

    /** F3 layout authored at 800px HUD height; scale spacing for 1200px (DISPLAY_SCALE 3). */
    private static final float HUD_LAYOUT_SCALE = Constants.HUD_HEIGHT_PX / 800f;

    private static final float TEXT_TIER_LINE = TextTier.SM.screenPx() + 4f;
    private static final float LINE_HEIGHT = TEXT_TIER_LINE * HUD_LAYOUT_SCALE;
    private static final float MARGIN_X = 8f * HUD_LAYOUT_SCALE;
    private static final float TEXT_TOP_INSET = 52f * HUD_LAYOUT_SCALE;
    private static final float KEY_BOX = 22f * HUD_LAYOUT_SCALE;
    private static final float KEY_GAP = 6f * HUD_LAYOUT_SCALE;
    private static final float KEY_MARGIN_X = 12f * HUD_LAYOUT_SCALE;
    private static final float KEY_MARGIN_TOP = 28f * HUD_LAYOUT_SCALE;

    private final HudAssets hud;
    private DebugMode mode = DebugMode.OFF;

    public DebugOverlay(HudAssets hud) {
        this.hud = hud;
    }

    public void cycleMode() {
        mode = mode.next();
    }

    public DebugMode getMode() {
        return mode;
    }

    public boolean isWorldDebugVisible() {
        return mode.showsWorldDebug();
    }

    public void render(
            InputController input,
            Entity entity,
            PlayerProfile profile,
            PlayerInventory inventory,
            EquipmentInventory equipment,
            float moveX,
            float moveY,
            float delta,
            String interactionMessage,
            long simTick,
            Boolean hoverSimActive,
            int screenX,
            int screenY,
            World world,
            BreakTarget hoverBreak) {
        if (!mode.showsHudDebug()) {
            return;
        }

        drawKeyIndicators(input, KEY_MARGIN_X, Constants.HUD_HEIGHT_PX - KEY_MARGIN_TOP);

        hud.batch.begin();
        float y = Constants.HUD_HEIGHT_PX - TEXT_TOP_INSET;

        y = line("F3: debug | [ ] scrub time | hold LMB mine / RMB place | 1-0 hotbar", MARGIN_X, y);

        CharacterSheet sheet = CharacterSheet.from(entity, inventory, equipment);
        WorldClock clock = world.clock();
        DayNightConfig dayNight = DayNightConfig.from(GameConfig.get());
        y = line(
                "Time: "
                        + clock.formatClock24h()
                        + "  Day "
                        + clock.dayIndex()
                        + " ("
                        + AmbientLighting.phaseLabel(clock.timeOfDay(), dayNight)
                        + ")  |  Sim tick: "
                        + simTick
                        + "  "
                        + (interactionMessage == null ? "" : interactionMessage),
                MARGIN_X,
                y);
        y = line(formatAttributes(sheet), MARGIN_X, y);
        y = line(formatVitals(sheet), MARGIN_X, y);
        y = line(formatSecondary(sheet), MARGIN_X, y);
        y = line(formatHiddenRates(sheet), MARGIN_X, y);
        y = line("Statuses: " + entity.getStatuses().formatDisplayNames(), MARGIN_X, y);
        if (profile != null) {
            y = line(
                    profile.name + "  Lv " + profile.level + "  XP " + profile.exp + "/" + profile.expToNext,
                    MARGIN_X,
                    y);
        }
        y = line(
                "Move: "
                        + fmt(moveX)
                        + ", "
                        + fmt(moveY)
                        + (input.isRunningWithEnergy(entity.getCurrentEnergy()) ? " RUN" : "")
                        + "  delta: "
                        + fmt(delta),
                MARGIN_X,
                y);
        y = line("Pos: " + fmt(entity.getX()) + ", " + fmt(entity.getY()), MARGIN_X, y);
        y = line("Mouse: " + screenX + ", " + screenY, MARGIN_X, y);
        y = line(formatHoverBlock(world, hoverBreak, hoverSimActive), MARGIN_X, y);
        y = line(
                formatLightStats(world, entity, hoverBreak, clock, dayNight),
                MARGIN_X,
                y);

        hud.batch.end();
    }

    private static String formatAttributes(CharacterSheet sheet) {
        StringBuilder sb = new StringBuilder("Attrs:");
        AttributeId[] ids = AttributeId.ALL;
        for (int i = 0; i < ids.length; i++) {
            sb.append(' ')
                    .append(AttributeId.DEBUG_ABBREV[i])
                    .append(' ')
                    .append(attributeValue(sheet, i));
        }
        return sb.toString();
    }

    private static int attributeValue(CharacterSheet sheet, int index) {
        return switch (index) {
            case 0 -> sheet.vitality();
            case 1 -> sheet.brawn();
            case 2 -> sheet.agility();
            case 3 -> sheet.focus();
            case 4 -> sheet.intellect();
            case 5 -> sheet.arcana();
            default -> 0;
        };
    }

    private static String formatVitals(CharacterSheet sheet) {
        return "HP "
                + fmtInt(sheet.currentHp())
                + "/"
                + fmtInt(sheet.maxHp())
                + "  Energy "
                + fmtInt(sheet.currentEnergy())
                + "/"
                + fmtInt(sheet.maxEnergy())
                + "  Hunger "
                + fmtInt(sheet.currentHunger())
                + "/"
                + fmtInt(sheet.maxHunger())
                + "  Thirst "
                + fmtInt(sheet.currentThirst())
                + "/"
                + fmtInt(sheet.maxThirst());
    }

    private static String formatSecondary(CharacterSheet sheet) {
        return "Armor "
                + fmtInt(sheet.armor())
                + "  Weight "
                + fmtWeight(sheet.currentWeight())
                + "/"
                + sheet.maxWeight()
                + "  Speed "
                + fmtInt(sheet.moveSpeed())
                + "  Dodge "
                + fmtInt(sheet.dodgePercent())
                + "%";
    }

    private static String formatHiddenRates(CharacterSheet sheet) {
        return "Rates HP"
                + fmtSignedRate(sheet.hpRegenPerSec())
                + "/s Energy"
                + fmtSignedRate(sheet.energyRegenPerSec())
                + "/s Hunger-"
                + fmt(sheet.hungerDrainPerSec())
                + "/s Thirst-"
                + fmt(sheet.thirstDrainPerSec())
                + "/s";
    }

    private static String fmtSignedRate(float rate) {
        if (rate >= 0f) {
            return "+" + fmt(rate);
        }
        return fmt(rate);
    }

    private void drawKeyIndicators(InputController input, float x, float y) {
        drawKeyBox(x + KEY_BOX + KEY_GAP, y + KEY_BOX + KEY_GAP, KEY_BOX, input, Input.Keys.W);
        drawKeyBox(x, y, KEY_BOX, input, Input.Keys.A);
        drawKeyBox(x + KEY_BOX + KEY_GAP, y, KEY_BOX, input, Input.Keys.S);
        drawKeyBox(x + (KEY_BOX + KEY_GAP) * 2, y, KEY_BOX, input, Input.Keys.D);
    }

    private void drawKeyBox(float x, float y, float size, InputController input, int key) {
        boolean on = input.isHeld(key);
        hud.shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        hud.shapes.setColor(on ? 0.2f : 0.15f, on ? 0.85f : 0.2f, on ? 0.3f : 0.2f, 0.9f);
        hud.shapes.rect(x, y, size, size);
        hud.shapes.end();
    }

    private float line(String text, float x, float y) {
        DawnTypography.draw(
                hud.batch,
                hud.fonts,
                hud.layout,
                text,
                TextTier.SM,
                TextContext.HUD,
                x,
                y,
                com.badlogic.gdx.graphics.Color.WHITE);
        return y - LINE_HEIGHT;
    }

    private static String fmt(float v) {
        return String.format("%.3f", v);
    }

    private static String fmtWeight(float v) {
        return String.format("%.1f", v);
    }

    private static String fmtInt(float v) {
        return String.valueOf(Math.round(v));
    }

    private static String formatLightStats(
            World world,
            Entity entity,
            BreakTarget hoverBreak,
            WorldClock clock,
            DayNightConfig dayNight) {
        int sampleX = hoverBreak != null ? hoverBreak.x() : (int) Math.floor(entity.getX());
        int sampleY = hoverBreak != null ? hoverBreak.y() : (int) Math.floor(entity.getY());
        float cellLight = world.lightMap().sample(sampleX, sampleY);
        float ambient = AmbientLighting.ambientLevel(clock.timeOfDay(), dayNight);
        String phase = AmbientLighting.phaseLabel(clock.timeOfDay(), dayNight);
        LightMap.HeldLightSource held = world.lightMap().heldSource();
        String heldStr = held == null
                ? "none"
                : String.format("r=%d e=%.2f rgb=(%.2f,%.2f,%.2f)", held.radius(), held.emission(), held.colorR(), held.colorG(), held.colorB());
        return "Light  cell("
                + sampleX + "," + sampleY + "): " + fmt(cellLight)
                + "  ambient: " + fmt(ambient) + " [" + phase + "]"
                + "  held: " + heldStr;
    }

    private static String formatHoverBlock(World world, BreakTarget hoverBreak, Boolean hoverSimActive) {
        if (hoverBreak == null) {
            return "Hover block: —";
        }
        float max = hoverBreak.health();
        float remaining = world.getBlockDamage().getRemaining(hoverBreak.layer(), hoverBreak.x(), hoverBreak.y(), max);
        String layer =
                switch (hoverBreak.layer()) {
                    case GROUND -> "ground";
                    case FLOOR -> "floor";
                    case OBJECT -> "object";
                };
        String sim = hoverSimActive == null ? "" : (hoverSimActive ? "  sim:active" : "  sim:OFF");
        return "Hover block: "
                + hoverBreak.blockId().name().toLowerCase()
                + " ("
                + layer
                + " "
                + hoverBreak.x()
                + ","
                + hoverBreak.y()
                + ")  hp "
                + fmt(remaining)
                + "/"
                + fmt(max)
                + sim;
    }

    @Override
    public void dispose() {}
}
