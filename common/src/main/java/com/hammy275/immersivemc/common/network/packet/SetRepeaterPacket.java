package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.NetworkUtil;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SetRepeaterPacket {

    protected final BlockPos pos;
    protected final int newDelay;

    public SetRepeaterPacket(BlockPos pos, int newDelay) {
        this.pos = pos;
        this.newDelay = newDelay;
    }

    public static void encode(SetRepeaterPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.newDelay);
    }

    public static SetRepeaterPacket decode(RegistryFriendlyByteBuf buffer) {
        return new SetRepeaterPacket(buffer.readBlockPos(), buffer.readInt());
    }

    public static void handle(SetRepeaterPacket message, ServerPlayer player) {
        if (!ActiveConfig.FILE_SERVER.useRepeaterImmersive) return;
        if (NetworkUtil.safeToRun(message.pos, player)) {
            if (message.newDelay >= 1 && message.newDelay <= 4) {
                Util.setRepeater(player.level(), message.pos, message.newDelay);
            }
        }
    }
}
