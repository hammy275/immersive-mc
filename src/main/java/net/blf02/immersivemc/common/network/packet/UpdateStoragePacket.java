package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.immersive.AbstractWorldStorageImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractWorldStorageInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateStoragePacket {

    public final BlockPos pos;
    public final ItemStack[] items;

    public UpdateStoragePacket(BlockPos pos, ItemStack[] newItems) {
        this.pos = pos;
        this.items = newItems;
    }

    public static void encode(UpdateStoragePacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.items.length);
        for (int i = 0; i < packet.items.length; i++) {
            buffer.writeItem(packet.items[i]);
        }
    }

    public static UpdateStoragePacket decode(PacketBuffer buffer) {
        BlockPos pos = buffer.readBlockPos();
        int length = buffer.readInt();
        ItemStack[] items = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            items[i] = buffer.readItem();
        }
        return new UpdateStoragePacket(pos, items);
    }

    public static void handle(final UpdateStoragePacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) {
                for (AbstractWorldStorageImmersive<? extends AbstractWorldStorageInfo> immersive : Immersives.WS_IMMERSIVES) {
                    for (AbstractWorldStorageInfo info : immersive.getTrackedObjects()) {
                        if (info.getBlockPosition().equals(message.pos)) {
                            immersive.processItems(info, message.items);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
