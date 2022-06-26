package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.NetworkClientHandlers;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.server.storage.GetStorage;
import net.blf02.immersivemc.server.storage.WorldStorage;
import net.blf02.immersivemc.common.storage.ImmersiveStorage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class FetchInventoryPacket {

    public final ItemStack[] items;
    public final BlockPos pos;

    public FetchInventoryPacket(BlockPos pos) {
        this(null, pos);
    }

    public FetchInventoryPacket(ItemStack[] items, BlockPos pos) {
        this.items = items;
        this.pos = pos;
    }

    public boolean isRequest() {
        return this.items != null;
    }

    public static void encode(FetchInventoryPacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.isRequest());
        if (packet.isRequest()) {
            buffer.writeInt(packet.items.length);
            for (ItemStack s : packet.items) {
                buffer.writeItem(s);
            }
        }
    }

    public static FetchInventoryPacket decode(PacketBuffer buffer) {
        BlockPos pos = buffer.readBlockPos();
        ItemStack[] stacks = null;
        if (buffer.readBoolean()) {
            int size = buffer.readInt();
            stacks = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                stacks[i] = buffer.readItem();
            }
        }
        return new FetchInventoryPacket(stacks, pos);
    }

    public static void handle(final FetchInventoryPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) { // Asking for inventory data
                handleServerToClient(player, message.pos);
            } else { // Receiving inventory data
                NetworkClientHandlers.handleReceiveInvData(message.items, message.pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void handleServerToClient(ServerPlayerEntity player, BlockPos pos) {
        if (NetworkUtil.safeToRun(pos, player)) {
            TileEntity tileEnt = player.level.getBlockEntity(pos);
            if (tileEnt != null) {
                IInventory inv;
                if (tileEnt instanceof IInventory) {
                    inv = (IInventory) tileEnt;
                } else if (tileEnt instanceof EnderChestTileEntity) {
                    inv = player.getEnderChestInventory();
                } else {
                    return;
                }
                ItemStack[] stacks = new ItemStack[inv.getContainerSize()];
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    stacks[i] = inv.getItem(i);
                }
                Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                        new FetchInventoryPacket(stacks, pos));
            } else if (WorldStorage.usesWorldStorage(player.level.getBlockState(pos))) {
                ImmersiveStorage storage = GetStorage.getStorage(player, pos);
                if (storage != null) {
                    Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                            new UpdateStoragePacket(pos, storage, storage.getType()));
                }
            }
        }
    }

}
