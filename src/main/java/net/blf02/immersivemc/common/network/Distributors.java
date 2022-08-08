package net.blf02.immersivemc.common.network;

import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.network.IPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Distributors {

    public static final PacketDistributor<NearbyDistributorData> NEARBY_POSITION =
            new PacketDistributor<>((packetDistributor, dataSupplier) -> nearbyPosition(dataSupplier), NetworkDirection.PLAY_TO_CLIENT);

    protected static Consumer<IPacket<?>> nearbyPosition(final Supplier<NearbyDistributorData> data) {
        return packet -> {
            MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.distanceToSqr(data.get().pos) <= data.get().distanceSqr) {
                    player.connection.connection.send(packet);
                }
            }
        };
    }

    public static class NearbyDistributorData {

        public final Vec3 pos;
        public final int distanceSqr;

        public NearbyDistributorData(Vec3 pos, int distance) {
            this.pos = pos;
            this.distanceSqr = distance * distance;
        }

        public NearbyDistributorData(BlockPos pos, int distance) {
            this(Vec3.atCenterOf(pos), distance);
        }
    }
}
