package com.dawn.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dawn.item.ItemId;
import com.dawn.item.ItemStack;
import com.dawn.world.block.BlockId;
import com.dawn.world.block.InteractionTag;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class InteractionRulesTest {
    @Test
    void toolError_noneTagAllowsHands() {
        assertNull(InteractionRules.toolError(ItemStack.empty(), EnumSet.of(InteractionTag.NONE)));
    }

    @Test
    void toolError_multiTagListsTools() {
        String msg = InteractionRules.toolError(ItemStack.empty(), EnumSet.of(InteractionTag.MINE, InteractionTag.CHOP));
        assertEquals("Need pickaxe or axe", msg);
    }

    @Test
    void toolError_pickaxeSatisfiesMine() {
        assertNull(InteractionRules.toolError(ItemStack.of(ItemId.PICKAXE), EnumSet.of(InteractionTag.MINE)));
    }
}
