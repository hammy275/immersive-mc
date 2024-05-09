package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.util.Util;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class UsePacket {

    public final BlockPos pos;

    public UsePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(UsePacket message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
    }

    public static UsePacket decode(FriendlyByteBuf buffer) {
        return new UsePacket(buffer.readBlockPos());
    }

    public static void handle(final UsePacket packet, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player != null && ImmersiveCheckers.isLever(packet.pos, player.level())) {
                Util.useLever(player, packet.pos);
            }
        });
    }
}
