package com.hammy275.immersivemc.common.network.packet;

import dev.architectury.networking.NetworkManager;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ImmersiveMCConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.server.PlayerConfigs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class ConfigSyncPacket {

    protected FriendlyByteBuf buffer;
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

    public ConfigSyncPacket(FriendlyByteBuf configBuffer) {
        this.buffer = configBuffer;
    }

    public static void encode(ConfigSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.kickMe); // Write if we're asking the server to kick us
        if (!packet.kickMe) { // If we don't want to kick us, write the config data
            if (packet.isToServerConfigPacket) {
                ActiveConfig.encodeServerOnlyConfig(buffer);
            } else {
                ImmersiveMCConfig.encode(buffer);
            }
        }
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) { // If we want to be kicked, just return a kick packet
            return getKickMePacket();
        }
        buffer.retain();
        return new ConfigSyncPacket(buffer); // Return a packet to be written to ActiveConfig
    }

    public static void handle(final ConfigSyncPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) { // If from the server, we just need to load the config
                ActiveConfig.loadConfigFromPacket(message.buffer);
                Network.INSTANCE.sendToServer(getToServerConfigPacket()); // Send server our preferences before we load its
            } else if (message.kickMe) { // If asking to be kicked, kick
                ((ServerPlayer) ctx.get().getPlayer()).connection.disconnect(
                        new TextComponent("The server is using a different version of ImmersiveMC than you!"));
            } else { // Get config from client
                PlayerConfigs.registerConfig(ctx.get().getPlayer(), message.buffer);
            }
        });
        
    }
}
