package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.vr.VRRumble;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class DoubleControllerVibrate {
    private final float duration;

    public DoubleControllerVibrate(float duration) {
        this.duration = duration;
    }

    public static void encode(DoubleControllerVibrate packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.duration);
    }

    public static DoubleControllerVibrate decode(FriendlyByteBuf buffer) {
        return new DoubleControllerVibrate(buffer.readFloat());
    }

    public static void handle(final DoubleControllerVibrate message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) { // Do vibrate
                VRRumble.rumbleIfVR(null, 0, message.duration);
                VRRumble.rumbleIfVR(null, 1, message.duration);
            }
        });

    }
}
