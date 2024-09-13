package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.common.network.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

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

    public static void handle(BufferPacket message, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            Network.INSTANCE.doReceive(ctx.getSender(), message.buffer);
        });
        ctx.setPacketHandled(true);
    }
}
