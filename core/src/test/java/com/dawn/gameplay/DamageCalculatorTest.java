package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dawn.entity.EntityManager;
import com.dawn.entity.EntityId;
import com.dawn.entity.Stats;
import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.test.TestStats;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.InteractionTag;
import com.dawn.world.block.Layer;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class DamageCalculatorTest {
    @Test
    void purposeDamage_brawn10Tool3_isEight() {
        Stats stats = TestStats.uniform(10);
        float dps = DamageCalculator.purposeDamagePerSec(
                stats, ItemStack.of(ItemId.SHOVEL), EnumSet.of(InteractionTag.DIG));
        assertEquals(8f, dps, 0.001f);
    }

    @Test
    void purposeDamage_wrongTool_isZero() {
        Stats stats = TestStats.uniform(10);
        float dps = DamageCalculator.purposeDamagePerSec(
                stats, ItemStack.of(ItemId.SHOVEL), EnumSet.of(InteractionTag.BREAK));
        assertEquals(0f, dps, 0.001f);
    }

    @Test
    void grabBreak_ignoresHeldToolDamage() {
        BreakTarget bush = new BreakTarget(0, 0, Layer.OBJECT, BlockId.BUSH, 8f);
        var entity = new EntityManager().spawn(EntityId.PLAYER, 0f, 0f);
        entity.getStats().setAllBase(10);

        float withShovel =
                DamageCalculator.damagePerSecForBreak(entity, ItemStack.of(ItemId.SHOVEL), bush);
        float emptyHand = DamageCalculator.damagePerSecForBreak(entity, ItemStack.empty(), bush);
        assertEquals(2f, withShovel, 0.001f);
        assertEquals(2f, emptyHand, 0.001f);
    }
}
