package net.blf02.immersivemc.common.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MegaPacket {

    private final List<Object> packets;

    public MegaPacket(List<Object> packets) {
        this.packets = packets;
    }

    public static void encode(MegaPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.packets.size());
        for (Object packet : message.packets) {
            Tuple<Integer, BiConsumer<Object, FriendlyByteBuf>> indexAndEncoder = NetworkHandler.INSTANCE.getEncoder(packet);
            buffer.writeInt(indexAndEncoder.getA());
            indexAndEncoder.getB().accept(packet, buffer);
        }
    }

    public static MegaPacket decode(FriendlyByteBuf buffer) {
        MegaPacket decoded = new MegaPacket(new LinkedList<>());
        int numOfPackets = buffer.readInt();
        for (int i = 0; i < numOfPackets; i++) {
            int index = buffer.readInt();
            Object packet = NetworkHandler.INSTANCE.getDecoder(index).apply(buffer);
            decoded.packets.add(packet);
        }
        return decoded;
    }

    public static void handle(final MegaPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        for (Object packet : message.packets) {
            NetworkHandler.INSTANCE.getHandler(packet).accept(packet, ctx);
        }
    }
}
