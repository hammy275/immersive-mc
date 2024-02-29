package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.GetStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class BeaconHandler extends WorldStorageHandler {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        ImmersiveStorage immersiveStorage = GetStorage.getBeaconStorage(player, pos);
        return new ListOfItemsStorage(Arrays.asList(immersiveStorage.getItemsRaw()), 1);
    }

    @Override
    public HandlerStorage getEmptyHandler() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        if (!player.getItemInHand(hand).is(ItemTags.BEACON_PAYMENT_ITEMS) && !player.getItemInHand(hand).isEmpty()) return;
        ImmersiveStorage beaconStorage = GetStorage.getBeaconStorage(player, pos);
        ItemStack beaconItem = beaconStorage.getItem(0);
        if (!beaconItem.isEmpty()) {
            Util.placeLeftovers(player, beaconItem);
            beaconStorage.setItem(0, ItemStack.EMPTY);
        }
        beaconStorage.placeItem(player, hand, 1, 0);
        beaconStorage.setDirty();
    }

    @Override
    public boolean usesWorldStorage() {
        return true;
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
    public ImmersiveStorage getStorage(ServerPlayer player, BlockPos pos) {
        return GetStorage.getBeaconStorage(player, pos);
    }
}
