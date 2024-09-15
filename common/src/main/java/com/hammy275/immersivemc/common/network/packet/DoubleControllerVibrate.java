package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class DoubleControllerVibrate {
    private final float duration;

    public DoubleControllerVibrate(float duration) {
        this.duration = duration;
    }

    public static void encode(DoubleControllerVibrate packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(packet.duration);
    }

    public static DoubleControllerVibrate decode(RegistryFriendlyByteBuf buffer) {
        return new DoubleControllerVibrate(buffer.readFloat());
    }

    public static void handle(final DoubleControllerVibrate message, ServerPlayer player) {
        if (player == null) { // Do vibrate
            NetworkClientHandlers.doDoubleRumble(message.duration);
        }
    }
}
