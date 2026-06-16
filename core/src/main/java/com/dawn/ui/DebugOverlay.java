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
import com.dawn.world.World;
import com.dawn.world.WorldClock;
import com.dawn.world.block.Layer;
import com.dawn.world.light.LightMap;

public class DebugOverlay implements Disposable {
    public static final int TOGGLE_KEY = Input.Keys.F3;

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

        drawKeyIndicators(input, 12f, Constants.HUD_HEIGHT_PX - 28f);

        hud.batch.begin();
        float y = Constants.HUD_HEIGHT_PX - 52f;
        float line = 20f;

        y = line("F3: debug | [ ] scrub time | hold LMB mine / RMB place | 1-0 hotbar", 8f, y, line);

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
                8f,
                y,
                line);
        y = line(formatAttributes(sheet), 8f, y, line);
        y = line(formatVitals(sheet), 8f, y, line);
        y = line(formatSecondary(sheet), 8f, y, line);
        y = line(formatHiddenRates(sheet), 8f, y, line);
        y = line("Statuses: " + entity.getStatuses().formatDisplayNames(), 8f, y, line);
        if (profile != null) {
            y = line(
                    profile.name + "  Lv " + profile.level + "  XP " + profile.exp + "/" + profile.expToNext,
                    8f,
                    y,
                    line);
        }
        y = line(
                "Move: "
                        + fmt(moveX)
                        + ", "
                        + fmt(moveY)
                        + (input.isRunningWithEnergy(entity.getCurrentEnergy()) ? " RUN" : "")
                        + "  delta: "
                        + fmt(delta),
                8f,
                y,
                line);
        y = line("Pos: " + fmt(entity.getX()) + ", " + fmt(entity.getY()), 8f, y, line);
        y = line("Mouse: " + screenX + ", " + screenY, 8f, y, line);
        y = line(formatHoverBlock(world, hoverBreak, hoverSimActive), 8f, y, line);
        y = line(
                formatLightStats(world, entity, hoverBreak, clock, dayNight),
                8f,
                y,
                line);

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
        float size = 22f;
        float gap = 6f;
        drawKeyBox(x + size + gap, y + size + gap, size, input, Input.Keys.W);
        drawKeyBox(x, y, size, input, Input.Keys.A);
        drawKeyBox(x + size + gap, y, size, input, Input.Keys.S);
        drawKeyBox(x + (size + gap) * 2, y, size, input, Input.Keys.D);
    }

    private void drawKeyBox(float x, float y, float size, InputController input, int key) {
        boolean on = input.isHeld(key);
        hud.shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        hud.shapes.setColor(on ? 0.2f : 0.15f, on ? 0.85f : 0.2f, on ? 0.3f : 0.2f, 0.9f);
        hud.shapes.rect(x, y, size, size);
        hud.shapes.end();
    }

    private float line(String text, float x, float y, float lineHeight) {
        DawnTypography.draw(
                hud.batch,
                hud.font,
                hud.layout,
                text,
                DawnTypography.TextTier.XS,
                DawnTypography.TextContext.HUD,
                x,
                y,
                com.badlogic.gdx.graphics.Color.WHITE);
        return y - lineHeight;
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
