package com.hammy275.immersivemc.test.tests;

import com.hammy275.immersivemc.common.immersive.handler.ETableHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.server.api_impl.ConstantItemSwapAmount;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.server.storage.world.WorldStorages;
import com.hammy275.immersivemc.server.storage.world.impl.ETableWorldStorage;
import com.hammy275.immersivemc.test.Test;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class ETableTest implements Test {

    BlockPos tablePos;
    ETableHandler handler;

    @Override
    public void setup(ServerPlayer player) {
        createTestingArea(player);
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        tablePos = fromVec3(player.position().add(4, 0, 0));
        WorldStorages.instance().remove(tablePos, level(player));
        setBlock(player, tablePos, Blocks.ENCHANTING_TABLE);
        fastForward(player, 2);
        handler = (ETableHandler) ImmersiveHandlers.enchantingTableHandler;
        DirtyTracker.unmarkAllDirty();
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 1; y++) {
                setBlock(player, tablePos.offset(x, y, -2), Blocks.BOOKSHELF);
                setBlock(player, tablePos.offset(x, y, 2), Blocks.BOOKSHELF);
            }
        }
        for (int z = -2; z <= 2; z++) {
            for (int y = 0; y <= 1; y++) {
                setBlock(player, tablePos.offset(-2, y, z), Blocks.BOOKSHELF);
                setBlock(player, tablePos.offset(2, y, z), Blocks.BOOKSHELF);

            }
        }
    }

    public void testCantGetAnythingWithNoLevels(ServerPlayer player) {
        player.setExperienceLevels(0);
        setItemInMainHand(player, Items.DIAMOND_SWORD);
        handler.swap(0, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
        ETableWorldStorage storage = (ETableWorldStorage) WorldStorages.instance().getWithoutVerification(tablePos, level(player));
        assertTrue(storage.isDirtyForClientSync());
        storage.setNoLongerDirtyForClientSync();

        player.getInventory().setItem(22, new ItemStack(Items.LAPIS_LAZULI, 64));
        for (int i = 1; i <= 3; i++) {
            handler.swap(1, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
            assertFalse(storage.isDirtyForClientSync());
            assertEmptyMainHand(player);
        }
    }

    public void testCantGetAnythingWithNoLapis(ServerPlayer player) {
        player.setExperienceLevels(999);
        setItemInMainHand(player, Items.DIAMOND_SWORD);
        handler.swap(0, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
        ETableWorldStorage storage = (ETableWorldStorage) WorldStorages.instance().getWithoutVerification(tablePos, level(player));
        assertTrue(storage.isDirtyForClientSync());
        storage.setNoLongerDirtyForClientSync();

        for (int i = 1; i <= 3; i++) {
            handler.swap(1, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
            assertFalse(storage.isDirtyForClientSync());
            assertEmptyMainHand(player);
        }
    }

    public void testEnchantWorks(ServerPlayer player) {
        for (int i = 1; i <= 3; i++) {
            setItemInMainHand(player, Items.DIAMOND_SWORD);
            handler.swap(0, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
            ETableWorldStorage storage = (ETableWorldStorage) WorldStorages.instance().getWithoutVerification(tablePos, level(player));
            assertTrue(storage.isDirtyForClientSync());
            storage.setNoLongerDirtyForClientSync();
            assertEquals(new ItemStack(Items.DIAMOND_SWORD), storage.getItem(0));

            player.setExperienceLevels(30);
            player.getInventory().setItem(22, new ItemStack(Items.LAPIS_LAZULI, i));
            handler.swap(i, InteractionHand.MAIN_HAND, tablePos, player, new ConstantItemSwapAmount(1));
            assertTrue(storage.isDirtyForClientSync());
            storage.setNoLongerDirtyForClientSync();
            assertEmpty(storage.getItem(0));
            assertEquals(30 - i, player.experienceLevel);
            assertEmpty(player.getInventory().getItem(22));
            assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEnchanted());
            assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.DIAMOND_SWORD));

        }
    }
}
