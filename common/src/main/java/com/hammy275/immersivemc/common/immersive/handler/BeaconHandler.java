package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.SwapResult;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.BeaconStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
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
        return (BeaconStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
    }

    @Override
    public BeaconStorage getEmptyNetworkStorage() {
        return new BeaconStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        ItemStack playerItem = player.getItemInHand(hand);
        if (!playerItem.is(ItemTags.BEACON_PAYMENT_ITEMS) && !playerItem.isEmpty()) return;
        BeaconStorage beaconStorage = (BeaconStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
        ItemStack beaconItem = beaconStorage.getItem(0);
        SwapResult result = ImmersiveLogicHelpers.instance().swapItems(playerItem, beaconItem, amount, player, 1);
        result.giveToPlayer(player, hand);
        beaconStorage.setItem(0, result.immersiveStack(), player);
        beaconStorage.setDirty(player.level());
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
        return Util.id("beacon");
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
