package net.blf02.immersivemc.common.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record PacketInfo<T>(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler) {
}
