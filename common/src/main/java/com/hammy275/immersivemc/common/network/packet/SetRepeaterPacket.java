package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import dev.architectury.networking.NetworkManager;

import java.util.function.Supplier;

public class SetRepeaterPacket {

    protected final BlockPos pos;
    protected final int newDelay;

    public SetRepeaterPacket(BlockPos pos, int newDelay) {
        this.pos = pos;
        this.newDelay = newDelay;
    }

    public static void encode(SetRepeaterPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.newDelay);
    }

    public static SetRepeaterPacket decode(FriendlyByteBuf buffer) {
        return new SetRepeaterPacket(buffer.readBlockPos(), buffer.readInt());
    }

    public static void handle(SetRepeaterPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            if (!ActiveConfig.useRepeaterImmersion) return;
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (NetworkUtil.safeToRun(message.pos, player)) {
                if (message.newDelay >= 1 && message.newDelay <= 4) {
                    Util.setRepeater(player.level, message.pos, message.newDelay);
                }
            }
        });
        
    }
}
