package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigSyncPacket {

    protected PacketBuffer buffer;
    protected boolean kickMe = false;

    public ConfigSyncPacket() {
    }

    protected ConfigSyncPacket(boolean kickMe) {
        this.kickMe = true;
    }

    public static ConfigSyncPacket getKickMePacket() {
        return new ConfigSyncPacket(true);
    }

    protected ConfigSyncPacket(PacketBuffer configBuffer) {
        this.buffer = configBuffer;
    }

    public static void encode(ConfigSyncPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.kickMe); // Write if we're asking the server to kick us
        if (!packet.kickMe) { // If we don't want to kick us, write the config data
            ImmersiveMCConfig.encode(buffer);
        }
    }

    public static ConfigSyncPacket decode(PacketBuffer buffer) {
        if (buffer.readBoolean()) { // If we want to be kicked, just return a kick packet
            return getKickMePacket();
        }
        return new ConfigSyncPacket(buffer); // Return a packet to be written to ActiveConfig
    }

    public static void handle(final ConfigSyncPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) { // If from the server, we just need to load the config
                ActiveConfig.loadConfigFromPacket(message.buffer);
            } else if (message.kickMe && ctx.get().getSender() != null) { // If asking to be kicked, kick
                ctx.get().getSender().connection.disconnect(
                        new StringTextComponent("The server is using a different vesrion of ImmersiveMC than you!"));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
