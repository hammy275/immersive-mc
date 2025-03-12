package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ClientActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.NetworkClientHandlers;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

    public static void encode(ConfigSyncPacket packet, RegistryFriendlyByteBuf buffer) {
        packet.config.encode(buffer);
        buffer.writeInt(packet.handlerIDs == null ? 0 : packet.handlerIDs.size());
        if (packet.handlerIDs != null) {
            packet.handlerIDs.forEach(buffer::writeResourceLocation);
        }
    }

    public static ConfigSyncPacket decode(RegistryFriendlyByteBuf buffer) {
        ActiveConfig incoming = ActiveConfig.decode(buffer);
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

    public static void handle(final ConfigSyncPacket message, ServerPlayer player) {
        if (player == null) { // S2C
            if (message.handlerIDs != null) {
                // Check handlers and kick on mismatch
                NetworkClientHandlers.checkHandlerMatch(message.handlerIDs);
            }
            // Load server config
            ActiveConfig.FROM_SERVER = message.config;
            ActiveConfig.loadActive();
            ClientUtil.clearDisabledImmersives();
            // Send server our config
            Network.INSTANCE.sendToServer(new ConfigSyncPacket(ActiveConfig.FILE_CLIENT));
        } else { // C2S sending us a config
            message.config.mergeWithServer(ActiveConfig.FILE_SERVER);
            ActiveConfig.registerPlayerConfig(player, (ClientActiveConfig) message.config);
            TrackedImmersives.clearForPlayer(player);
        }
    }

    public static void syncConfigToPlayer(ServerPlayer player) {
        Network.INSTANCE.sendToPlayer(player,
                new ConfigSyncPacket(ImmersiveMCPlayerStorages.isPlayerDisabled(player) ? ActiveConfig.DISABLED : ActiveConfig.FILE_SERVER));
    }
}
