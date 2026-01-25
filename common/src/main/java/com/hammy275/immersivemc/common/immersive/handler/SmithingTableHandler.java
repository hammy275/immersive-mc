package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.ItemStorage;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.SmithingTableStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SmithingTableBlock;

public class SmithingTableHandler extends ItemWorldStorageHandler<SmithingTableStorage> {
    @Override
    public SmithingTableStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return (SmithingTableStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
    }

    @Override
    public SmithingTableStorage getEmptyNetworkStorage() {
        return new SmithingTableStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        SmithingTableStorage storage = (SmithingTableStorage) WorldStoragesImpl.getOrCreateS(pos, player.level());
        if (slot != 3) {
            storage.placeItem(player, hand, slot, amount);
            storage.setItem(3, ItemStack.EMPTY);
            if (!storage.getItem(0).isEmpty() && !storage.getItem(1).isEmpty() && !storage.getItem(2).isEmpty()) {
                ItemStack output = Swap.getSmithingTableOutput(storage.getItem(0),
                        storage.getItem(1), storage.getItem(2), player);
                storage.setItem(3, output);
            }
        } else if (!storage.getItem(3).isEmpty()) { // Craft our result!
            boolean res = Swap.handleSmithingTableCraft(storage, pos, player, hand);
            if (res) {
                VRRumble.rumbleIfVR(player, hand, CommonConstants.vibrationTimeWorldInteraction);
            }
        }
        storage.setDirty(player.level());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).getBlock() instanceof SmithingTableBlock;
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useSmithingTableImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("smithing_table");
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
