package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DoETablePacket {

    public final int slot;
    public final Hand hand;
    public final BlockPos pos;
    public final int power;

    public DoETablePacket(int slot, Hand hand, BlockPos pos, int power) {
        this.slot = slot;
        this.hand = hand;
        this.pos = pos;
        this.power = power;
        if (power < 1 || power > 3) {
            throw new IllegalArgumentException("1 for upper slot, 2 for middle slot, 3 for bottom slot");
        }
    }

    public static void encode(DoETablePacket packet, PacketBuffer buffer) {
        buffer.writeInt(packet.slot).writeBoolean(packet.hand == Hand.MAIN_HAND);
        buffer.writeBlockPos(packet.pos).writeInt(packet.power);
    }

    public static DoETablePacket decode(PacketBuffer buffer) {
        return new DoETablePacket(buffer.readInt(), buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND, buffer.readBlockPos(),
                buffer.readInt());
    }

    public static void handle(DoETablePacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (NetworkUtil.safeToRun(message.pos, player)) {
                if (player.level.getBlockState(message.pos).getBlock() instanceof EnchantingTableBlock) {
                    Swap.handleETable(message.slot, message.pos, player, message.hand, message.power);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
