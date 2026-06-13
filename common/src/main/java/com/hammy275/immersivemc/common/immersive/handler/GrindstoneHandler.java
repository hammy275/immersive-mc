package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.dual.impl.GrindstoneStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.server.storage.world.WorldStorage;
import com.hammy275.immersivemc.server.storage.world.WorldStoragesImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class GrindstoneHandler extends ItemWorldStorageHandler<GrindstoneStorage> {

    @Override
    public GrindstoneStorage makeInventoryContents(ServerPlayer tracker, BlockPos pos) {
        return (GrindstoneStorage) WorldStoragesImpl.getOrCreateS(pos, tracker.serverLevel());
    }

    @Override
    public WorldStorage getEmptyWorldStorage() {
        return new GrindstoneStorage();
    }

    @Override
    public Class<? extends WorldStorage> getWorldStorageClass() {
        return GrindstoneStorage.class;
    }

    @Override
    public GrindstoneStorage getEmptyNetworkStorage() {
        return new GrindstoneStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer tracker, ItemSwapAmount amount) {
        GrindstoneStorage storage = makeInventoryContents(tracker, pos);
        ItemStack stackIn = tracker.getItemInHand(hand).copy();
        GrindstoneMenu menu = new GrindstoneMenu(-1, tracker.getInventory(), ContainerLevelAccess.create(tracker.level(), pos));
        if (slot == 0 || slot == 1) {
            if (menu.getSlot(slot).mayPlace(stackIn) || tracker.getItemInHand(hand).isEmpty()) {
                ItemStack stackToPlayer = storage.getItem(slot);
                tracker.setItemInHand(hand, stackToPlayer.copy());
                storage.setItem(slot, stackIn, tracker);
                for (int i = 0; i <= 1; i++) {
                    menu.setItem(i, 0, storage.getItem(i));
                }
                storage.setItem(2, menu.getSlot(2).getItem().copy());
            }
        } else if (slot == 2 && !storage.getItem(2).isEmpty()) {
            for (int i = 0; i <= 1; i++) {
                menu.setItem(i, 0, storage.getItem(i));
            }
            Util.giveStackHandFirst(tracker, hand, storage.getItem(2).copy());
            menu.getSlot(2).onTake(tracker, storage.getItem(2));
            for (int i = 0; i <= 2; i++) {
                storage.setItem(i, ItemStack.EMPTY);
            }
        } else if (slot == 3) { // VR grindstone movement action.
            menu.setItem(0, 0, stackIn);
            if (!menu.getSlot(2).getItem().isEmpty()) {
                tracker.setItemInHand(hand, menu.getSlot(2).getItem().copy());
                menu.getSlot(2).onTake(tracker, storage.getItem(2));
            }
        }
        storage.setDirty(tracker.serverLevel());
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return level.getBlockState(pos).is(Blocks.GRINDSTONE);
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useGrindstoneImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("grindstone");
    }
}
