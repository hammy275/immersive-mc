package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BufferPacket(RegistryFriendlyByteBuf buffer) implements CustomPacketPayload {

    public static final Type<BufferPacket> ID = new Type<>(Util.id("network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BufferPacket> CODEC =
            CustomPacketPayload.codec(BufferPacket::write, BufferPacket::read);

    public static BufferPacket read(RegistryFriendlyByteBuf buffer) {
        return new BufferPacket(new RegistryFriendlyByteBuf(buffer.readBytes(buffer.readInt()), buffer.registryAccess()));
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.buffer.readableBytes());
        buffer.writeBytes(this.buffer);
        this.buffer.resetReaderIndex();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
