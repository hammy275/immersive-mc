package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.WorldStorage;
import com.hammy275.immersivemc.server.storage.WorldStorages;
import com.hammy275.immersivemc.server.storage.impl.BeaconWorldStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import java.util.Arrays;

public class BeaconHandler extends ItemWorldStorageHandlerImpl {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        BeaconWorldStorage beaconStorage = (BeaconWorldStorage) WorldStorages.get(pos, player.serverLevel());
        return new ListOfItemsStorage(Arrays.asList(beaconStorage.getItemsRaw()), 1);
    }

    @Override
    public HandlerStorage getEmptyHandlerStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        if (!player.getItemInHand(hand).is(ItemTags.BEACON_PAYMENT_ITEMS) && !player.getItemInHand(hand).isEmpty()) return;
        BeaconWorldStorage beaconStorage = (BeaconWorldStorage) WorldStorages.get(pos, player.serverLevel());
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
        return new BeaconWorldStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return BeaconWorldStorage.class;
    }

}
