package com.hammy275.immersivemc.common.network.packet;

import dev.architectury.networking.NetworkManager;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Syncs config data and information about the ImmersiveMC state between the server and client.
 * Starts with an S2C packet, then a C2S packet afterward during the initial handshake.
 * <br>
 * Server and client can update their respective config states as well at anytime by sending this packet without
 * the handlerIDs.
 */
public class ConfigSyncPacket {

    public final ActiveConfig config;
    @Nullable
    public final List<ResourceLocation> handlerIDs;

    public ConfigSyncPacket(ActiveConfig config) {
        this(config, null);
    }

    public ConfigSyncPacket(ActiveConfig config, @Nullable List<ResourceLocation> handlerIDs) {
        this.config = config;
        this.handlerIDs = handlerIDs;
    }

    public static void encode(ConfigSyncPacket packet, FriendlyByteBuf buffer) {
        packet.config.encode(buffer);
        buffer.writeInt(packet.handlerIDs == null ? 0 : packet.handlerIDs.size());
        if (packet.handlerIDs != null) {
            packet.handlerIDs.forEach(buffer::writeResourceLocation);
        }
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buffer) {
        ActiveConfig incoming = new ActiveConfig();
        incoming.decode(buffer);
        List<ResourceLocation> handlerIDs = null;
        int numIDs = buffer.readInt();
        if (numIDs > 0) {
            handlerIDs = new ArrayList<>();
            for (int i = 0; i < numIDs; i++) {
                handlerIDs.add(buffer.readResourceLocation());
            }
        }
        return new ConfigSyncPacket(incoming, handlerIDs);
    }

    public static void handle(final ConfigSyncPacket message, Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            ServerPlayer player = ctx.get().getPlayer() instanceof ServerPlayer ? (ServerPlayer) ctx.get().getPlayer() : null;
            if (player == null) { // S2C
                // Check handlers and kick on mismatch
                NetworkClientHandlers.checkHandlerMatch(message.handlerIDs);
                // Load server config
                ActiveConfig.FROM_SERVER = message.config;
                ActiveConfig.loadActive();
                // Send server our config
                Network.INSTANCE.sendToServer(new ConfigSyncPacket(ActiveConfig.FILE));
            } else { // C2S sending us a config
                message.config.mergeWithOther(ActiveConfig.FILE);
                ActiveConfig.registerPlayerConfig(ctx.get().getPlayer(), message.config);
                TrackedImmersives.clearForPlayer(player);
            }
        });
    }
}
