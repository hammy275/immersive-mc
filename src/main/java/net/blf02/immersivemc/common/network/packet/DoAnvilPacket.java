package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DoAnvilPacket {

    public final int left;
    public final int mid;
    public final BlockPos pos;
    public final Hand hand;

    public DoAnvilPacket(int slotLeft, int slotMid, BlockPos pos, Hand hand) {
        this.left = slotLeft;
        this.mid = slotMid;
        this.pos = pos;
        this.hand = hand;
    }

    public static void encode(DoAnvilPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.left).writeInt(packet.mid);
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
    }

    public static DoAnvilPacket decode(PacketBuffer buffer) {
        return new DoAnvilPacket(buffer.readInt(), buffer.readInt(), buffer.readBlockPos(),
                buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static void handle(DoAnvilPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ActiveConfig.useAnvilImmersion) return;
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender != null) { // Client asking us to craft something
                if (!NetworkUtil.safeToRun(message.pos, sender)) { // Make sure we're in a reasonable distance
                    return;
                }
                Swap.handleAnvil(message.left, message.mid, message.pos, sender, message.hand);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
