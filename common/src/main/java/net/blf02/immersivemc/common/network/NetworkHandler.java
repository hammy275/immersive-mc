package net.blf02.immersivemc.common.network;

import dev.architectury.networking.NetworkChannel;
import dev.architectury.networking.NetworkManager;
import net.blf02.immersivemc.ImmersiveMC;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {

    public static final NetworkHandler INSTANCE = new NetworkHandler();

    private final List<PacketInfo<?>> packetInfos = new ArrayList<>();

    private final NetworkChannel channel = NetworkChannel.create(
            new ResourceLocation(ImmersiveMC.MOD_ID, "immersive_mc"));

    private final List<Object> toServer = new LinkedList<>();
    private final Map<ServerPlayer, List<Object>> toClients = new HashMap<>();

    private NetworkHandler() {
        channel.register(MegaPacket.class, MegaPacket::encode, MegaPacket::decode, MegaPacket::handle);
    }

    public void sendToServer(Object message) {
        toServer.add(message);
    }

    public void sendToPlayer(ServerPlayer player, Object message) {
        if (!toClients.containsKey(player)) {
            toClients.put(player, new LinkedList<>());
        }
        toClients.get(player).add(message);
    }

    public void sendToPlayers(Iterable<ServerPlayer> players, Object message) {
        for (ServerPlayer player : players) {
            sendToPlayer(player, message);
        }
    }

    public <T> void register(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler) {
        packetInfos.add(new PacketInfo<>(type, encoder, decoder, handler));
    }

    public Tuple<Integer, BiConsumer<Object, FriendlyByteBuf>> getEncoder(Object packet) {
        for (int i = 0; i < this.packetInfos.size(); i++) {
            PacketInfo<?> info = this.packetInfos.get(i);
            if (info.type().isInstance(packet)) {
                // Everything is an object, so this should hopefully work
                return new Tuple<>(i, (BiConsumer<Object, FriendlyByteBuf>) info.encoder());
            }
        }
        throw new IllegalArgumentException("No encoder defined for class " + packet.getClass());
    }

    public Function<FriendlyByteBuf, Object> getDecoder(int index) {
        return (Function<FriendlyByteBuf, Object>) packetInfos.get(index).decoder();
    }

    public BiConsumer<Object, Supplier<NetworkManager.PacketContext>> getHandler(Object packet) {
        for (int i = 0; i < this.packetInfos.size(); i++) {
            PacketInfo<?> info = this.packetInfos.get(i);
            if (info.type().isInstance(packet)) {
                return (BiConsumer<Object, Supplier<NetworkManager.PacketContext>>) info.handler();
            }
        }
        throw new IllegalArgumentException("No handler defined for class " + packet.getClass());
    }

    public void tick(boolean isClientSide) {
        if (isClientSide) {
            channel.sendToServer(new MegaPacket(this.toServer));
            this.toServer.clear();
        } else {
            for (Map.Entry<ServerPlayer, List<Object>> playerPacketPair : this.toClients.entrySet()) {
                channel.sendToPlayer(playerPacketPair.getKey(), new MegaPacket(playerPacketPair.getValue()));
            }
            this.toClients.clear();
        }
    }


}
