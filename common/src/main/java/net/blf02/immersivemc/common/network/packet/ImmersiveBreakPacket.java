package net.blf02.immersivemc.common.network.packet;

import dev.architectury.networking.NetworkManager;
import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class ImmersiveBreakPacket {

    public final BlockPos pos;

    public ImmersiveBreakPacket(AbstractImmersiveInfo info) {
        this.pos = info.getBlockPosition();
    }

    public ImmersiveBreakPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(ImmersiveBreakPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static ImmersiveBreakPacket decode(FriendlyByteBuf buffer) {
        return new ImmersiveBreakPacket(buffer.readBlockPos());
    }

    public static <I extends AbstractImmersiveInfo> void handle(final ImmersiveBreakPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) { // From server to client
                boolean breakOut = false;
                for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                    for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                        if (
                                info.getBlockPosition().equals(message.pos) || // if the position matches our thing
                                (info instanceof ChestInfo && ((ChestInfo) info).other != null &&
                                        ((ChestInfo) info).other.getBlockPos().equals(message.pos)) // If the position is the other chest
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
        
    }
}
