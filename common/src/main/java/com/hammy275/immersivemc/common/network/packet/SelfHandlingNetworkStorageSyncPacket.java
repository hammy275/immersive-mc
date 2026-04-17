package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.immersive.storage.network.SelfHandlingNetworkStorage;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestOpennessStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record SelfHandlingNetworkStorageSyncPacket(SelfHandlingNetworkStorage storage) {

    public static void encode(SelfHandlingNetworkStorageSyncPacket message, RegistryFriendlyByteBuf buffer) {
        message.storage.encode(buffer);
    }

    public static SelfHandlingNetworkStorageSyncPacket decode(RegistryFriendlyByteBuf buffer) {
        // TODO: Eventually, I'd like to expand this system to support other network storage types.
        //       This would require registration somewhere, so for now, just hardcode the one type we need.
        ChestOpennessStorage storage = new ChestOpennessStorage();
        storage.decode(buffer);
        return new SelfHandlingNetworkStorageSyncPacket(storage);
    }

    public static void handle(final SelfHandlingNetworkStorageSyncPacket packet, ServerPlayer player) {
        if (player instanceof ServerPlayer sp) {
            packet.storage.handleServer(sp);
        } else {
            packet.storage.handleClient();
        }
    }
}
