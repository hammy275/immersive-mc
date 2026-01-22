package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.compat.apotheosis.Apoth;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ApothSalvagingTableStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;

public class ApothSalvagingTableHandler extends ItemWorldStorageHandler<ApothSalvagingTableStorage> {
    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new ApothSalvagingTableStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return ApothSalvagingTableStorage.class;
    }

    @Override
    public ApothSalvagingTableStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return (ApothSalvagingTableStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
    }

    @Override
    public ApothSalvagingTableStorage getEmptyNetworkStorage() {
        return new ApothSalvagingTableStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        ApothSalvagingTableStorage storage = (ApothSalvagingTableStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
        if (slot < 9) { // Placing input
            ItemStack handItem = player.getItemInHand(hand);
            if (handItem.isEmpty() || Apoth.apothImpl.isSalvagable(handItem, player.level())) {
                storage.placeItem(player, hand, slot, amount, 1);
            }
        } else { // Doing craft to outputs
            List<ItemStack> outputs = Apoth.apothImpl.doSalvage(player, Arrays.stream(storage.getItemsRaw()).toList(), pos);
            for (ItemStack stack : outputs) {
                Util.placeLeftovers(player, stack, Vec3.atBottomCenterOf(pos).add(0, 1, 0));
            }
            for (int i = 0; i < storage.getNumItems(); i++) {
                storage.setItem(i, ItemStack.EMPTY);
            }
        }
        storage.setDirty(player.level());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return Apoth.apothImpl.isSalvagingTable(level, pos);
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useApotheosisSalvagingTableImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("apoth_salvaging_table");
    }
}
