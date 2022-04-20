package net.blf02.immersivemc.common.network.packet;

import com.mojang.datafixers.util.Pair;
import net.blf02.immersivemc.client.storage.ClientStorage;
import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class GetAnvilOutputPacket {

    public boolean isAsking; // true if left and mid are defined, false if right and levels are defined

    public ItemStack left;
    public ItemStack mid;

    public ItemStack right;
    public int levels;

    public boolean isReallyAnvil;

    public GetAnvilOutputPacket(ItemStack left, ItemStack mid, boolean isReallyAnvil) {
        this.left = left;
        this.mid = mid;
        this.isAsking = true;
        this.isReallyAnvil = isReallyAnvil;

    }

    public GetAnvilOutputPacket(ItemStack right, int levels, boolean isReallyAnvil) {
        this.right = right;
        this.levels = levels;
        this.isAsking = false;
        this.isReallyAnvil = isReallyAnvil;
    }

    public static void encode(GetAnvilOutputPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.isAsking);
        if (packet.isAsking) {
            buffer.writeItem(packet.left);
            buffer.writeItem(packet.mid);
        } else {
            buffer.writeItem(packet.right);
            buffer.writeInt(packet.levels);
        }
        buffer.writeBoolean(packet.isReallyAnvil);
    }

    public static GetAnvilOutputPacket decode(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            return new GetAnvilOutputPacket(buffer.readItem(), buffer.readItem(), buffer.readBoolean());
        } else {
            return new GetAnvilOutputPacket(buffer.readItem(), buffer.readInt(), buffer.readBoolean());
        }
    }

    public static void handle(final GetAnvilOutputPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ActiveConfig.useAnvilImmersion) return;
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null) { // Write to ClientStorage
                addAnvilOutput(message.right, message.levels, message.isReallyAnvil);
            } else { // Calculate output and exp cost, and send info back
                Pair<ItemStack, Integer> output = Swap.getAnvilOutput(message.left, message.mid, message.isReallyAnvil, sender);
                Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender),
                        new GetAnvilOutputPacket(output.getFirst(), output.getSecond(), message.isReallyAnvil));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    protected static void addAnvilOutput(ItemStack right, int cost, boolean isReallyAnvil) {
        if (isReallyAnvil) {
            ClientStorage.anvilStorage[2] = right;
            ClientStorage.anvilCost = cost;
        } else {
            ClientStorage.smithingStorage[2] = right;
        }
    }
}
