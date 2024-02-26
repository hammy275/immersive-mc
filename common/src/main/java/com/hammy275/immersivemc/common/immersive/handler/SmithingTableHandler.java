package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SmithingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class SmithingTableHandler extends WorldStorageHandler {
    @Override
    public HandlerStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        ImmersiveStorage immersiveStorage = GetStorage.getSmithingTableStorage(player, pos);
        return new ListOfItemsStorage(Arrays.asList(immersiveStorage.getItemsRaw()), 4);
    }

    @Override
    public HandlerStorage getEmptyHandler() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, PlacementMode mode) {
        ImmersiveStorage storage = GetStorage.getSmithingTableStorage(player, pos);
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
        storage.setDirty();
    }

    @Override
    public boolean usesWorldStorage() {
        return true;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, BlockState state, BlockEntity blockEntity, Level level) {
        return state.getBlock() instanceof SmithingTableBlock;
    }

    @Override
    public boolean enabledInServerConfig() {
        return ActiveConfig.FILE.useSmithingTableImmersion;
    }

    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(ImmersiveMC.MOD_ID, "smithing_table");
    }

    @Override
    public ImmersiveStorage getStorage(ServerPlayer player, BlockPos pos) {
        return GetStorage.getSmithingTableStorage(player, pos);
    }
}
