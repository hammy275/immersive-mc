package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.MultiblockBlockBasedImmersiveHandler;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestOpennessStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SelfHandlingNetworkStorageSyncPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.server.SharedNetworkStorages;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ChestHandler extends ChestLikeHandler<ListOfItemsStorage>
        implements MultiblockBlockBasedImmersiveHandler<ListOfItemsStorage>, AfterClientSyncHandler {

    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer tracker, BlockPos pos) {
        BlockEntity blockEntity = tracker.level.getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity cbe) {
            ListOfItemsStorage storage = makeBaseInventoryContents(tracker, pos);
            ChestBlockEntity otherChest = Util.getOtherChest(cbe);
            if (otherChest != null) {
                ListOfItemsStorage otherStorage = makeBaseInventoryContents(tracker, otherChest.getBlockPos());
                storage.getItems().addAll(otherStorage.getItems());
            }
            return storage;
        } else { // Is an ender chest
            // NOTE: On (1.19.2) Forge, PlayerEnderChestContainer#items is private; hence why we for loop here
            // instead of just initializing directly from the items list.
            List<ItemStack> items = new ArrayList<>(tracker.getEnderChestInventory().getContainerSize());
            for (int i = 0; i < tracker.getEnderChestInventory().getContainerSize(); i++) {
                items.add(tracker.getEnderChestInventory().getItem(i));
            }
            return new ListOfItemsStorage(items, 27);
        }
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer tracker, ItemSwapAmount amount) {
        BlockEntity blockEntity = tracker.level.getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity cbe) {
            Swap.handleChest(cbe, tracker, hand, slot);
        } else if (blockEntity instanceof EnderChestBlockEntity) {
            Swap.handleEnderChest(tracker, hand, slot);
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer tracker, BlockPos pos) {
        BlockEntity blockEntity = tracker.level.getBlockEntity(pos);
        if (blockEntity instanceof EnderChestBlockEntity) {
            return tracker.tickCount % 2 == 0; // Every other tick for dirtiness. Not ideal, but works.
        } else {
            boolean isDirtyForClientSync = super.isDirtyForClientSync(tracker, pos);
            ChestBlockEntity otherChest = Util.getOtherChest((ChestBlockEntity) tracker.level.getBlockEntity(pos));
            if (otherChest != null) {
                isDirtyForClientSync = isDirtyForClientSync || super.isDirtyForClientSync(tracker, otherChest.getBlockPos());
            }
            return isDirtyForClientSync;
        }
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return getBaseEmptyNetworkStorage();
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return (blockEntity instanceof ChestBlockEntity || blockEntity instanceof EnderChestBlockEntity) &&
                super.isValidBlock(pos, level);
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useChestImmersive;
    }

    @Override
    public ResourceLocation getID() {
        return Util.id("chest");
    }

    @Override
    public @Nullable Set<BlockPos> getHandledBlocks(BlockPos pos, Level level) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EnderChestBlockEntity) {
            return Set.of(pos);
        } else if (be instanceof ChestBlockEntity cbe) {
            ChestBlockEntity other = Util.getOtherChest(cbe);
            if (other != null) {
                return Set.of(pos, other.getBlockPos());
            } else {
                return Set.of(pos);
            }
        }
        return null;
    }

    @Override
    public void afterClientSync(ServerPlayer player, Set<BlockPos> positions) {
        // Use the minimum value in the set so the position for large chests is consistent.
        BlockPos pos = positions.stream().min(BlockPos::compareTo).get();
        BlockEntity blockEntity = player.level.getBlockEntity(pos);
        ChestOpennessStorage storage = SharedNetworkStorages.instance().getOrCreate(player.level, pos,
                ChestOpennessStorage.class, () -> new ChestOpennessStorage(blockEntity));
        Network.INSTANCE.sendToPlayer(player, new SelfHandlingNetworkStorageSyncPacket(storage));
    }
}