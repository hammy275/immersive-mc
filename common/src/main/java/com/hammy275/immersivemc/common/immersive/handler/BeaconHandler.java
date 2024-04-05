package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStorages;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class BeaconHandler extends ItemWorldStorageHandler {
    @Override
    public NetworkStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return (NetworkStorage) WorldStorages.getOrCreate(pos, player.serverLevel());
    }

    @Override
    public NetworkStorage getEmptyNetworkStorage() {
        return new BeaconStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        if (!player.getItemInHand(hand).is(ItemTags.BEACON_PAYMENT_ITEMS) && !player.getItemInHand(hand).isEmpty()) return;
        BeaconStorage beaconStorage = (BeaconStorage) WorldStorages.getOrCreate(pos, player.serverLevel());
        ItemStack beaconItem = beaconStorage.getItem(0);
        if (!beaconItem.isEmpty()) {
            Util.placeLeftovers(player, beaconItem);
            beaconStorage.setItem(0, ItemStack.EMPTY);
        }
        beaconStorage.placeItem(player, hand, 1, 0);
        beaconStorage.setDirty(player.serverLevel());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockEntity(pos) instanceof BeaconBlockEntity;
    }

    @Override
    public boolean enabledInConfig(ActiveConfig config) {
        return config.useBeaconImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "beacon");
    }

    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new BeaconStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return BeaconStorage.class;
    }

}
