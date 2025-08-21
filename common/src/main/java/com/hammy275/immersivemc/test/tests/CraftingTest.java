package com.hammy275.immersivemc.test.tests;

import com.hammy275.immersivemc.common.immersive.handler.CraftingHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.CraftingTableStorage;
import com.hammy275.immersivemc.server.api_impl.ConstantItemSwapAmount;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.server.storage.world.WorldStorages;
import com.hammy275.immersivemc.test.Test;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class CraftingTest implements Test {

    BlockPos tablePos;
    CraftingHandler handler;

    @Override
    public void setup(ServerPlayer player) {
        createTestingArea(player);
        tablePos = fromVec3(player.position().add(2, 0, 0));
        setBlock(player, tablePos, Blocks.CRAFTING_TABLE);
        fastForward(player, 1);
        handler = (CraftingHandler) ImmersiveHandlers.craftingHandler;
        DirtyTracker.unmarkAllDirty();
    }

    public void testCraftItem(ServerPlayer player) {
        setItemInMainHand(player, Items.STICK);
        handler.swap(7, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
        setItemInMainHand(player, Items.COAL);
        handler.swap(4, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
        CraftingTableStorage storage = (CraftingTableStorage) WorldStorages.instance().getWithoutVerification(tablePos, level(player));
        assertEmpty(storage.getItem(0));
        assertEmpty(storage.getItem(1));
        assertEmpty(storage.getItem(2));
        assertEmpty(storage.getItem(3));
        assertEquals(new ItemStack(Items.COAL), storage.getItem(4));
        assertEmpty(storage.getItem(5));
        assertEmpty(storage.getItem(6));
        assertEquals(new ItemStack(Items.STICK), storage.getItem(7));
        assertEmpty(storage.getItem(8));
        assertEquals(new ItemStack(Items.TORCH, 4), storage.getItem(9));
        assertEmptyMainHand(player);

        handler.swap(9, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
        for (int i = 0; i <= 9; i++) {
            assertEmpty(storage.getItem(i));
        }
        assertEquals(new ItemStack(Items.TORCH, 4), player.getItemInHand(InteractionHand.MAIN_HAND));
    }

}
