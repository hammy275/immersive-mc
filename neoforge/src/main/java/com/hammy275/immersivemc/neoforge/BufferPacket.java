package com.hammy275.immersivemc.neoforge;

import com.hammy275.immersivemc.common.network.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public class BufferPacket {
    private final FriendlyByteBuf buffer;

    public BufferPacket(FriendlyByteBuf buffer) {
        this.buffer = buffer;
    }

    public static void encode(BufferPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.buffer.readableBytes());
        buffer.writeBytes(message.buffer);
        message.buffer.resetReaderIndex();
    }

    public static BufferPacket decode(FriendlyByteBuf buffer) {
        return new BufferPacket(new FriendlyByteBuf(buffer.readBytes(buffer.readInt())));
    }

    public static void handle(BufferPacket message, NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            Network.INSTANCE.doReceive(ctx.getSender(), message.buffer);
        });
        ctx.setPacketHandled(true);
    }
}
