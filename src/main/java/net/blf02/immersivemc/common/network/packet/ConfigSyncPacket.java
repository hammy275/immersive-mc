package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.config.ImmersiveMCConfig;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.server.PlayerConfigs;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigSyncPacket {

    protected PacketBuffer buffer;
    protected boolean kickMe = false;
    protected boolean isToServerConfigPacket = false;

    public ConfigSyncPacket() {
    }

    protected ConfigSyncPacket(boolean kickMe) {
        this.kickMe = true;
    }

    public static ConfigSyncPacket getKickMePacket() {
        return new ConfigSyncPacket(true);
    }

    public static ConfigSyncPacket getToServerConfigPacket() {
        ConfigSyncPacket packet = new ConfigSyncPacket();
        packet.isToServerConfigPacket = true;
        return packet;
    }

    public ConfigSyncPacket(PacketBuffer configBuffer) {
        this.buffer = configBuffer;
    }

    public static void encode(ConfigSyncPacket packet, PacketBuffer buffer) {
        buffer.writeBoolean(packet.kickMe); // Write if we're asking the server to kick us
        if (!packet.kickMe) { // If we don't want to kick us, write the config data
            if (packet.isToServerConfigPacket) {
                ActiveConfig.encodeServerOnlyConfig(buffer);
            } else {
                ImmersiveMCConfig.encode(buffer);
            }
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
                Network.INSTANCE.sendToServer(getToServerConfigPacket());
            } else if (message.kickMe && ctx.get().getSender() != null) { // If asking to be kicked, kick
                ctx.get().getSender().connection.disconnect(
                        new TextComponent("The server is using a different version of ImmersiveMC than you!"));
            } else if (ctx.get().getSender() != null) { // Get config from client
                PlayerConfigs.registerConfig(ctx.get().getSender(), message.buffer);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
