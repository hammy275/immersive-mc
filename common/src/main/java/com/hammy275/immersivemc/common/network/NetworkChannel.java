package com.hammy275.immersivemc.common.network;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.client.ClientUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

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
        Platform.sendToServer(encode(message, ClientUtil.getRegistryAccess()));
    }

    public <T> void sendToPlayer(ServerPlayer player, T message) {
        Platform.sendToPlayer(player, encode(message, player.registryAccess()));
    }

    public <T> void sendToPlayers(Iterable<ServerPlayer> players, T message) {
        players.forEach(p -> sendToPlayer(p, message));
    }

    @SuppressWarnings("unchecked")
    public <T> void doReceive(@Nullable ServerPlayer player, RegistryFriendlyByteBuf buffer) {
        NetworkRegistrationData<T> data = (NetworkRegistrationData<T>) packets.get(buffer.readInt());
        T message;
        try {
            message = data.decoder.apply(buffer);
        } catch (Exception e) {
            ImmersiveMC.LOGGER.log(Level.ERROR, "Error while decoding packet.", e);
            return;
        }
        data.handler.accept(message, player);
    }

    private <T> RegistryFriendlyByteBuf encode(T message, RegistryAccess access) {
        NetworkRegistrationData<T> data = getData(message);
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), access);
        buffer.writeInt(data.id());
        data.encoder().accept(message, buffer);
        return buffer;
    }

    @SuppressWarnings("unchecked")
    private <T> NetworkRegistrationData<T> getData(T message) {
        NetworkRegistrationData<T> data = (NetworkRegistrationData<T>) packets.stream()
                .filter(d -> d.clazz == message.getClass())
                .findFirst().orElse(null);
        if (data == null) {
            throw new IllegalArgumentException("Packet type %s not registered!".formatted(message.getClass().getName()));
        }
        return data;
    }

    public record NetworkRegistrationData<T>(int id, Class<T> clazz, BiConsumer<T, RegistryFriendlyByteBuf> encoder,
                                             Function<RegistryFriendlyByteBuf, T> decoder, BiConsumer<T, ServerPlayer> handler) {}
}
