package com.hammy275.immersivemc.test.tests;

import com.hammy275.immersivemc.common.immersive.handler.FurnaceHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.server.api_impl.ConstantItemSwapAmount;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.test.Test;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

/**
 * Tests functionality of furnace handler.
 */
public class FurnaceTest implements Test {

    BlockPos furnacePos;
    FurnaceHandler handler;
    FurnaceBlockEntity furnace;

    @Override
    public void setup(ServerPlayer player) {
        createTestingArea(player);
        player.setGameMode(GameType.CREATIVE);
        furnacePos = fromVec3(player.position().add(2, 0, 0));
        setBlock(player, furnacePos, Blocks.FURNACE);
        fastForward(player, 1);
        handler = (FurnaceHandler) ImmersiveHandlers.furnaceHandler;
        furnace = (FurnaceBlockEntity) level(player).getBlockEntity(furnacePos);
        DirtyTracker.unmarkAllDirty();
    }

    public void testPlaceValidItemInInput(ServerPlayer player) {
        // Test smeltable item can be placed
        setItemInMainHand(player, Items.OAK_LOG);
        handler.swap(0, InteractionHand.MAIN_HAND, furnacePos, player, new ConstantItemSwapAmount(1));
        assertEquals(new ItemStack(Items.OAK_LOG), furnace.getItem(0));
        assertEmptyMainHand(player);
        assertTrue(handler.isDirtyForClientSync(player, furnacePos));
    }

    public void testPlaceInvalidItemInInput(ServerPlayer player) {
        // Test unsmeltable item can be placed
        setItemInMainHand(player, Items.DIAMOND_HORSE_ARMOR);
        handler.swap(0, InteractionHand.MAIN_HAND, furnacePos, player, new ConstantItemSwapAmount(1));
        assertEquals(new ItemStack(Items.DIAMOND_HORSE_ARMOR), furnace.getItem(0));
        assertEmptyMainHand(player);
        assertTrue(handler.isDirtyForClientSync(player, furnacePos));
    }

    public void testPlaceFuelInFuel(ServerPlayer player) {
        // Test fuel item can be placed
        setItemInMainHand(player, Items.OAK_LOG);
        handler.swap(1, InteractionHand.MAIN_HAND, furnacePos, player, new ConstantItemSwapAmount(1));
        assertEquals(new ItemStack(Items.OAK_LOG), furnace.getItem(1));
        assertEmptyMainHand(player);
        assertTrue(handler.isDirtyForClientSync(player, furnacePos));
    }

    public void testCantPlaceNonFuelInFuel(ServerPlayer player) {
        // Test fuel item can't be placed
        setItemInMainHand(player, Items.DIAMOND_HORSE_ARMOR);
        handler.swap(1, InteractionHand.MAIN_HAND, furnacePos, player, new ConstantItemSwapAmount(1));
        assertEmpty(furnace.getItem(1));
        assertEquals(new ItemStack(Items.DIAMOND_HORSE_ARMOR), player.getItemInHand(InteractionHand.MAIN_HAND));
        assertFalse(handler.isDirtyForClientSync(player, furnacePos));
    }

    public void testCanGetSmeltingOutput(ServerPlayer player) {
        setItemInMainHand(player, Items.OAK_LOG);
        handler.swap(0, InteractionHand.MAIN_HAND, furnacePos, player, new ConstantItemSwapAmount(1));
        setItemInMainHand(player, Items.OAK_LOG);
        handler.swap(1, InteractionHand.MAIN_HAND, furnacePos, player, new ConstantItemSwapAmount(1));
        fastForward(player, 200);
        handler.swap(2, InteractionHand.MAIN_HAND, furnacePos, player, new ConstantItemSwapAmount(1));
        assertEmpty(furnace.getItem(0));
        assertEmpty(furnace.getItem(1));
        assertEmpty(furnace.getItem(2));
        assertEquals(new ItemStack(Items.CHARCOAL), player.getItemInHand(InteractionHand.MAIN_HAND));
        assertTrue(handler.isDirtyForClientSync(player, furnacePos));
    }
}
