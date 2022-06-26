package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.NetworkUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@Deprecated
public class ClientDoAnvilPacket {

    public final int left;
    public final int mid;
    public final BlockPos pos;
    public final Hand hand;

    public ClientDoAnvilPacket(int slotLeft, int slotMid, BlockPos pos, Hand hand) {
        this.left = slotLeft;
        this.mid = slotMid;
        this.pos = pos;
        this.hand = hand;
    }

    public static void encode(ClientDoAnvilPacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.left).writeInt(packet.mid);
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.hand == Hand.MAIN_HAND);
    }

    public static ClientDoAnvilPacket decode(PacketBuffer buffer) {
        return new ClientDoAnvilPacket(buffer.readInt(), buffer.readInt(), buffer.readBlockPos(),
                buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static void handle(ClientDoAnvilPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ActiveConfig.useAnvilImmersion) return;
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender != null) { // Client asking us to craft something
                if (!NetworkUtil.safeToRun(message.pos, sender)) { // Make sure we're in a reasonable distance
                    return;
                }
                //Swap.handleAnvilCraft(message.left, message.mid, message.pos, sender, message.hand);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
