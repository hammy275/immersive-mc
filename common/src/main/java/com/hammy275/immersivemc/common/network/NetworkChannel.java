package com.hammy275.immersivemc.common.network;

import com.hammy275.immersivemc.Platform;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NetworkChannel {
    private final List<NetworkRegistrationData<?>> packets = new ArrayList<>();

    public <T> void register(Class<T> clazz, BiConsumer<T, RegistryFriendlyByteBuf> encoder,
                             Function<RegistryFriendlyByteBuf, T> decoder, BiConsumer<T, ServerPlayer> handler) {
        packets.add(new NetworkRegistrationData<>(packets.size(), clazz, encoder, decoder, handler));
    }

    public <T> void sendToServer(T message) {
        Platform.COMMON.sendToServer(new NetworkPacket<>(message, getRegistrationData(message)));
    }

    public <T> void sendToPlayer(ServerPlayer player, T message) {
        Platform.COMMON.sendToPlayer(player, new NetworkPacket<>(message, getRegistrationData(message)));
    }

    public <T> void sendToPlayers(Iterable<ServerPlayer> players, T message) {
        players.forEach(p -> sendToPlayer(p, message));
    }

    @SuppressWarnings("unchecked")
    public <T> NetworkChannel.NetworkRegistrationData<T> getRegistrationData(T message) {
        NetworkChannel.NetworkRegistrationData<T> data = (NetworkChannel.NetworkRegistrationData<T>) packets.stream()
                .filter(d -> d.clazz == message.getClass())
                .findFirst().orElse(null);
        if (data == null) {
            throw new IllegalArgumentException("Packet type %s not registered!".formatted(message.getClass().getName()));
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public <T> NetworkChannel.NetworkRegistrationData<T> getRegistrationData(int index) {
        return (NetworkRegistrationData<T>) packets.get(index);
    }

    public record NetworkRegistrationData<T>(int id, Class<T> clazz, BiConsumer<T, RegistryFriendlyByteBuf> encoder,
                                             Function<RegistryFriendlyByteBuf, T> decoder, BiConsumer<T, ServerPlayer> handler) {}
}
