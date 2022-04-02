package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ImmersiveBreakPacket {

    public final BlockPos pos;

    public ImmersiveBreakPacket(AbstractImmersiveInfo info) {
        this.pos = info.getBlockPosition();
    }

    public ImmersiveBreakPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(ImmersiveBreakPacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static ImmersiveBreakPacket decode(PacketBuffer buffer) {
        return new ImmersiveBreakPacket(buffer.readBlockPos());
    }

    public static <I extends AbstractImmersiveInfo> void handle(final ImmersiveBreakPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) { // From server to client
                boolean breakOut = false;
                for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                    for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                        if (
                                info.getBlockPosition().equals(message.pos) || // if the position matches our thing
                                (info instanceof ChestInfo && ((ChestInfo) info).other != null &&
                                        ((ChestInfo) info).other.getBlockPos().equals(message.pos)) // If the position is
                        ) {
                            singleton.getTrackedObjects().remove(info);
                            breakOut = true;
                            break;
                        }
                    }
                    if (breakOut) break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
