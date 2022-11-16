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
    private final Map<Class<?>, Integer> packetInfosMap = new HashMap<>();

    private final NetworkChannel channel = NetworkChannel.create(
            new ResourceLocation(ImmersiveMC.MOD_ID, "immersive_mc"));

    private final List<Object> toServer = new LinkedList<>();
    private final Map<ServerPlayer, List<Object>> toClients = new HashMap<>();

    private int registerIndex = 0;

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
        packetInfosMap.put(type, registerIndex++);
    }

    public Tuple<Integer, BiConsumer<Object, FriendlyByteBuf>> getEncoder(Object packet) {
        int index = this.packetInfosMap.get(packet.getClass());
        return new Tuple<>(index, (BiConsumer<Object, FriendlyByteBuf>) this.packetInfos.get(index).encoder());
    }

    public Function<FriendlyByteBuf, Object> getDecoder(int index) {
        return (Function<FriendlyByteBuf, Object>) packetInfos.get(index).decoder();
    }

    public BiConsumer<Object, Supplier<NetworkManager.PacketContext>> getHandler(Object packet) {
        int index = this.packetInfosMap.get(packet.getClass());
        return (BiConsumer<Object, Supplier<NetworkManager.PacketContext>>) this.packetInfos.get(index).handler();
    }

    // Called at the very end of every tick to send mega packets around.
    public void tick(boolean isClientSide) {
        if (isClientSide) {
            if (this.toServer.size() > 0) {
                channel.sendToServer(new MegaPacket(this.toServer));
                this.toServer.clear();
            }
        } else {
            for (Map.Entry<ServerPlayer, List<Object>> playerPacketPair : this.toClients.entrySet()) {
                if (playerPacketPair.getValue().size() > 0) {
                    channel.sendToPlayer(playerPacketPair.getKey(), new MegaPacket(playerPacketPair.getValue()));
                }
            }
            this.toClients.clear();
        }
    }


}
