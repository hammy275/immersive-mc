package com.hammy275.immersivemc.common.network;

import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record NetworkPacket<T>(T data, NetworkChannel.NetworkRegistrationData<T> registrationData) implements CustomPacketPayload {

    // network is used by the older 1.20.5+ packet system which stored a byte buffer directly
    public static final Type<NetworkPacket> ID = new Type<>(Util.id("network2"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkPacket> CODEC =
            CustomPacketPayload.codec(NetworkPacket::write, NetworkPacket::read);

    public static <T> NetworkPacket<T> read(RegistryFriendlyByteBuf buffer) {
        NetworkChannel.NetworkRegistrationData<T> registrationData = Network.INSTANCE.getRegistrationData(buffer.readInt());
        return new NetworkPacket<>(registrationData.decoder().apply(buffer), registrationData);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(registrationData.id());
        registrationData.encoder().accept(data, buffer);
    }

    public void handle(@Nullable ServerPlayer player) {
        registrationData.handler().accept(data, player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
