package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.api.server.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class BeaconHandler extends ItemWorldStorageHandler<BeaconStorage> {
    @Override
    public BeaconStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return (BeaconStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
    }

    @Override
    public BeaconStorage getEmptyNetworkStorage() {
        return new BeaconStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        if (!player.getItemInHand(hand).is(ItemTags.BEACON_PAYMENT_ITEMS) && !player.getItemInHand(hand).isEmpty()) return;
        BeaconStorage beaconStorage = (BeaconStorage) WorldStoragesImpl.getOrCreateS(pos, player.serverLevel());
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
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useBeaconImmersive;
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
