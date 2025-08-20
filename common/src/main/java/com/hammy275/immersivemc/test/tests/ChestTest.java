package com.hammy275.immersivemc.test.tests;

import com.hammy275.immersivemc.common.immersive.handler.ChestHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.server.api_impl.ConstantItemSwapAmount;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.test.Test;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class ChestTest implements Test {

    BlockPos chestPos;
    ChestHandler handler;
    ChestBlockEntity chest;

    @Override
    public void setup(ServerPlayer player) {
        createTestingArea(player);
        chestPos = fromVec3(player.position().add(2, 0, 0));
        setBlock(player, chestPos, Blocks.CHEST);
        fastForward(player, 1);
        handler = (ChestHandler) ImmersiveHandlers.chestHandler;
        chest = (ChestBlockEntity) level(player).getBlockEntity(chestPos);
        DirtyTracker.unmarkAllDirty();
    }

    public void testSingleChestSwap(ServerPlayer player) {
        setItemInMainHand(player, Items.STICK);
        handler.swap(2, InteractionHand.MAIN_HAND, chestPos, player, new ConstantItemSwapAmount(1));
        assertEmptyMainHand(player);
        assertEquals(chest.getItem(2), new ItemStack(Items.STICK));
        assertTrue(handler.isDirtyForClientSync(player, chestPos));
    }

}
