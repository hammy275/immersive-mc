package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SmithingTableBlock;

public class SmithingTableHandler extends ItemWorldStorageHandler<SmithingTableStorage> {
    @Override
    public SmithingTableStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return (SmithingTableStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
    }

    @Override
    public SmithingTableStorage getEmptyNetworkStorage() {
        return new SmithingTableStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        SmithingTableStorage storage = (SmithingTableStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
        if (slot != 3) {
            storage.placeItem(player, hand, Swap.getPlaceAmount(player.getItemInHand(hand), mode), slot);
            storage.setItem(3, ItemStack.EMPTY);
            if (!storage.getItem(0).isEmpty() && !storage.getItem(1).isEmpty() && !storage.getItem(2).isEmpty()) {
                ItemStack output = Swap.getSmithingTableOutput(storage.getItem(0),
                        storage.getItem(1), storage.getItem(2), player);
                storage.setItem(3, output);
            }
        } else if (!storage.getItem(3).isEmpty()) { // Craft our result!
            if (!player.getItemInHand(hand).isEmpty()) return;
            boolean res = Swap.handleSmithingTableCraft(storage, pos, player, hand);
            if (res) {
                VRRumble.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
            }
        }
        storage.setDirty(player.serverLevel());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof SmithingTableBlock;
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useSmithingTableImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "smithing_table");
    }

    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new SmithingTableStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return SmithingTableStorage.class;
    }

    @Override
    public void updateStorageOutputAfterItemReturn(ServerPlayer player, BlockPos pos, ItemStorage storage) {
        ItemStack out = Swap.getSmithingTableOutput(storage.getItem(0), storage.getItem(1),
                storage.getItem(2), player);
        storage.setItem(3, out);
    }
}
