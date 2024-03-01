package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.storage.AnvilWorldStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;

import java.util.Arrays;

public class AnvilHandler extends WorldStorageHandler {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        AnvilWorldStorage worldStorage = GetStorage.getAnvilStorage(player, pos);
        return new AnvilStorage(Arrays.asList(worldStorage.getItemsRaw()), worldStorage.xpLevels);
    }

    @Override
    public HandlerStorage getEmptyHandler() {
        return new AnvilStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        AnvilWorldStorage storage = GetStorage.getAnvilStorage(player, pos);
        if (slot != 2) {
            storage.placeItem(player, hand, Swap.getPlaceAmount(player.getItemInHand(hand), mode), slot);
            storage.setItem(2, ItemStack.EMPTY);
            storage.xpLevels = 0;
            if (!storage.getItem(0).isEmpty() && !storage.getItem(1).isEmpty()) {
                Pair<ItemStack, Integer> output = Swap.getAnvilOutput(storage.getItem(0), storage.getItem(1), player);
                storage.setItem(2, output.getFirst());
                storage.xpLevels = output.getSecond();
            }
        } else if (!storage.getItem(2).isEmpty()) { // Craft our result!
            if (!player.getItemInHand(hand).isEmpty()) return;
            boolean res = Swap.handleAnvilCraft(storage, pos, player, hand);
            if (res) {
                VRRumble.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
            }
        }
        storage.setDirty();
    }

    @Override
    public boolean usesWorldStorage() {
        return true;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof AnvilBlock;
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useAnvilImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "anvil");
    }

    @Override
    public ImmersiveStorage getStorage(ServerPlayer player, BlockPos pos) {
        return GetStorage.getAnvilStorage(player, pos);
    }
}
