package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.immersive.*;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.storage.ImmersiveStorage;
import com.hammy275.immersivemc.server.storage.GetStorage;
import com.hammy275.immersivemc.server.storage.ImmersiveMCLevelStorage;
import com.hammy275.immersivemc.server.swap.Swap;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

public class FetchInventoryPacket {

    public final HandlerStorage storage;
    public final ImmersiveHandler handler;
    public final BlockPos pos;

    public FetchInventoryPacket(BlockPos pos) {
        this(null, null, pos);
    }

    public FetchInventoryPacket(ItemStack[] items, BlockPos pos) {
        this(null, new ListOfItemsStorage(Arrays.stream(items).toList(), items.length), pos);
    }

    public FetchInventoryPacket(@Nullable ImmersiveHandler handler, HandlerStorage storage, BlockPos pos) {
        this.handler = handler;
        this.storage = storage;
        this.pos = pos;
    }

    public boolean hasData() {
        return this.storage != null;
    }

    public static void encode(FetchInventoryPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.hasData());
        if (packet.hasData()) {
            ResourceLocation id = packet.handler == null ? new ResourceLocation(ImmersiveMC.MOD_ID, "generic") : packet.handler.getID();
            buffer.writeResourceLocation(id);
            packet.storage.encode(buffer);
        }
    }

    public static FetchInventoryPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ImmersiveHandler handlerToSet = null;
        HandlerStorage storage = null;
        ResourceLocation id;
        if (buffer.readBoolean()) {
            id = buffer.readResourceLocation();
            if (id.getPath().equals("generic")) {
                // TODO: Remove. Used to support ItemStack arrays as we move to the new system
                storage = new ListOfItemsStorage();
                storage.decode(buffer);
            } else {
                for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
                    if (handler.getID().equals(id)) {
                        handlerToSet = handler;
                        storage = handlerToSet.getEmptyHandler();
                        storage.decode(buffer);
                        break;
                    }
                }
                if (storage == null) {
                    throw new IllegalArgumentException("ID " + id + " not found!");
                }
            }
        }
        return new FetchInventoryPacket(handlerToSet, storage, pos);
    }

    public static void handle(final FetchInventoryPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player != null) { // Asking for inventory data
                handleServerToClient(player, message.pos);
            } else { // Receiving inventory data
                NetworkClientHandlers.handleReceiveInvData(message.storage, message.pos, message.handler == null ?
                        new ResourceLocation(ImmersiveMC.MOD_ID, "generic") : message.handler.getID());
            }
        });
        
    }

    public static void handleServerToClient(ServerPlayer player, BlockPos pos) {
        if (NetworkUtil.safeToRun(pos, player)) {
            BlockEntity tileEnt = player.level().getBlockEntity(pos);
            for (ImmersiveHandler handler : ImmersiveHandlers.HANDLERS) {
                if (handler.isValidBlock(pos, player.level().getBlockState(pos), tileEnt, player.level())) {
                    HandlerStorage storage = handler.makeInventoryContents(player, pos);
                    Network.INSTANCE.sendToPlayer(player, new FetchInventoryPacket(handler, storage, pos));
                    return;
                }
            }
            if (ImmersiveMCLevelStorage.usesWorldStorage(pos, player.level().getBlockState(pos), tileEnt, player.level())) {
                ImmersiveStorage storage = GetStorage.getStorage(player, pos);
                if (storage != null) {
                    Network.INSTANCE.sendToPlayer(player,
                            new UpdateStoragePacket(pos, storage, storage.getType()));
                }
            } else if (tileEnt != null) {
                Container inv;
                Container lootrInv = Lootr.lootrImpl.getContainer(player, pos);
                if (lootrInv != null) {
                    inv = lootrInv;
                } else if (tileEnt instanceof Container) {
                    inv = (Container) tileEnt;
                } else if (tileEnt instanceof EnderChestBlockEntity) {
                    inv = player.getEnderChestInventory();
                } else {
                    return;
                }
                int extra = 0;
                boolean isTCCraftingStation = ImmersiveCheckers.isTinkersConstructCraftingStation(pos, player.level().getBlockState(pos), tileEnt, player.level());
                if (isTCCraftingStation) {
                    extra = 1;
                }
                ItemStack[] stacks = new ItemStack[inv.getContainerSize() + extra];
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    stacks[i] = inv.getItem(i);
                }
                if (isTCCraftingStation) {
                    stacks[9] = Swap.getRecipeOutput(player, stacks);
                }
                Network.INSTANCE.sendToPlayer(player,
                        new FetchInventoryPacket(stacks, pos));
            }
        }
    }

}
