package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.common.vr.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetRepeaterPacket {

    protected final BlockPos pos;
    protected final int newDelay;

    public SetRepeaterPacket(BlockPos pos, int newDelay) {
        this.pos = pos;
        this.newDelay = newDelay;
    }

    public static void encode(SetRepeaterPacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.newDelay);
    }

    public static SetRepeaterPacket decode(PacketBuffer buffer) {
        return new SetRepeaterPacket(buffer.readBlockPos(), buffer.readInt());
    }

    public static void handle(SetRepeaterPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ActiveConfig.useRepeaterImmersion) return;
            ServerPlayerEntity player = ctx.get().getSender();
            if (NetworkUtil.safeToRun(message.pos, player)) {
                if (message.newDelay >= 1 && message.newDelay <= 4) {
                    Util.setRepeater(player.level, message.pos, message.newDelay);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
