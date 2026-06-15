package com.dawn.gameplay;

import com.dawn.entity.Entity;
import com.dawn.entity.StatFormulas;
import com.dawn.entity.Stats;
import com.dawn.item.ItemDef;
import com.dawn.item.ItemRegistry;
import com.dawn.item.ItemStack;
import com.dawn.world.block.BlockDefinitions;
import com.dawn.world.block.BlockDefinitions.BlockDef;
import com.dawn.world.block.InteractionTag;
import java.util.Set;

/** Shared damage resolution for tools, grab, and future combat. */
public final class DamageCalculator {
    private DamageCalculator() {}

    /** DPS while breaking {@code target} with {@code held} wielded by {@code entity}. */
    public static float damagePerSecForBreak(Entity entity, ItemStack held, BreakTarget target) {
        BlockDef def = BlockDefinitions.get(target.blockId());
        if (def == null) {
            return 0f;
        }
        Set<InteractionTag> tags = def.breakTags();
        if (tags != null && tags.contains(InteractionTag.NONE)) {
            return StatFormulas.grabDamagePerSec(entity.getStats());
        }
        return purposeDamagePerSec(entity.getStats(), held, tags);
    }

    /** DPS for dig/mine/chop when the held tool matches one of the block's required tags. */
    public static float purposeDamagePerSec(Stats wielder, ItemStack held, Set<InteractionTag> requiredTags) {
        ItemDef tool = ItemRegistry.get(held);
        if (tool == null || !tool.matchesAny(requiredTags)) {
            return 0f;
        }
        return physicalDamagePerSec(wielder, tool);
    }

    /** Melee DPS shared by purpose breaks and physical combat. */
    public static float physicalDamagePerSec(Stats wielder, ItemDef weapon) {
        return StatFormulas.meleeBonus(wielder) + weaponDamage(weapon);
    }

    private static float weaponDamage(ItemDef weapon) {
        return weapon == null ? 0f : weapon.weaponDamage();
    }
}
