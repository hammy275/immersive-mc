package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class ConfigSyncPacket {

    public boolean kickMe;
    public ActiveConfig config;

    public ConfigSyncPacket(boolean isKickMe) {
        kickMe = isKickMe;
        config = null;
    }

    public ConfigSyncPacket(ActiveConfig config) {
        kickMe = false;
        this.config = config;
    }

    public static void encode(ConfigSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.kickMe); // Write if we're asking the server to kick us
        if (!packet.kickMe) { // If we don't want to kick us, write the config data
            packet.config.encode(buffer);
        }
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) { // If we want to be kicked, just decode a kick packet
            return new ConfigSyncPacket(true);
        } else {
            ActiveConfig incoming = new ActiveConfig();
            incoming.decode(buffer);
            return new ConfigSyncPacket(incoming);
        }
    }

    public static void handle(final ConfigSyncPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) { // S2C
                // Load server config
                ActiveConfig.FROM_SERVER = message.config;
                ActiveConfig.loadActive();
                // Send server our config
                Network.INSTANCE.sendToServer(new ConfigSyncPacket(ActiveConfig.FILE));
            } else if (message.kickMe) { // C2S asking to be kicked
                ((ServerPlayer) ctx.get().getPlayer()).connection.disconnect(
                        Component.literal("The server is using a different version of ImmersiveMC than you!"));
            } else { // C2S sending us a config
                message.config.mergeWithOther(ActiveConfig.FILE);
                ActiveConfig.registerPlayerConfig(ctx.get().getPlayer(), message.config);
            }
        });
        
    }
}
