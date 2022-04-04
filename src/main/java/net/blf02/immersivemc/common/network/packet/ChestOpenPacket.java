package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.network.NetworkUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ChestOpenPacket {

    public BlockPos pos;
    public boolean isOpen;

    public ChestOpenPacket(BlockPos pos, boolean isOpenPacket) {
        this.pos = pos;
        this.isOpen = isOpenPacket;
    }

    public static void encode(ChestOpenPacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos).writeBoolean(packet.isOpen);
    }

    public static ChestOpenPacket decode(PacketBuffer buffer) {
        return new ChestOpenPacket(buffer.readBlockPos(), buffer.readBoolean());
    }

    public static void handle(final ChestOpenPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                if (NetworkUtil.safeToRun(message.pos, player)) {
                    TileEntity tileEnt = player.level.getBlockEntity(message.pos);
                    if (tileEnt instanceof ChestTileEntity) {
                        ChestTileEntity chest = (ChestTileEntity) tileEnt;
                        if (message.isOpen) {
                            chest.startOpen(player);
                        } else {
                            chest.stopOpen(player);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }


}
