package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.AnvilStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import com.hammy275.immersivemc.server.swap.Swap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;

public class AnvilHandler extends ItemWorldStorageHandler<AnvilStorage> {
    @Override
    public AnvilStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return (AnvilStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
    }

    @Override
    public AnvilStorage getEmptyNetworkStorage() {
        return new AnvilStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        AnvilStorage storage = (AnvilStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
        if (slot != 2) {
            storage.placeItem(player, hand, slot, amount);
            storage.setItem(2, ItemStack.EMPTY);
            storage.xpLevels = 0;
            if (!storage.getItem(0).isEmpty() && !storage.getItem(1).isEmpty()) {
                Pair<ItemStack, Integer> output = Swap.getAnvilOutput(storage.getItem(0), storage.getItem(1), player);
                storage.setItem(2, output.getFirst());
                storage.xpLevels = output.getSecond();
            }
        } else if (!storage.getItem(2).isEmpty()) { // Craft our result!
            boolean res = Swap.handleAnvilCraft(storage, pos, player, hand);
            if (res) {
                VRRumble.rumbleIfVR(player, hand, CommonConstants.vibrationTimeWorldInteraction);
            }
        }
        storage.setDirty(player.level());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof AnvilBlock;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useAnvilImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("anvil");
    }

    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new AnvilStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return AnvilStorage.class;
    }

    @Override
    public void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos, ItemStorage storageIn) {
        AnvilStorage storage = (AnvilStorage) storageIn;
        Pair<ItemStack, Integer> out = Swap.getAnvilOutput(storage.getItem(0), storage.getItem(1), player);
        storage.xpLevels = out.getSecond();
        storage.setItem(2, out.getFirst());
    }
}
