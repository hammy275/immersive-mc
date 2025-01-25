package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class UsePacket {

    public final BlockPos pos;

    public UsePacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(UsePacket message, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.pos);
    }

    public static UsePacket decode(RegistryFriendlyByteBuf buffer) {
        return new UsePacket(buffer.readBlockPos());
    }

    public static void handle(final UsePacket packet, ServerPlayer player) {
        if (player != null) {
            Level level = player.level();
            if (ImmersiveCheckers.isLever(packet.pos, level)) {
                Util.useLever(player, packet.pos);
            } else if (ImmersiveHandlers.trapdoorHandler.isValidBlock(packet.pos, level)) {
                Util.useTrapdoor(player, level, packet.pos);
            } else if (ImmersiveHandlers.doorHandler.isValidBlock(packet.pos, level)) {
                Util.useDoor(player, level, packet.pos);
            }
        }
    }
}
