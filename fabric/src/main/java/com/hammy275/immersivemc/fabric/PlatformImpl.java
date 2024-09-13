package com.hammy275.immersivemc.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.util.function.Consumer;

public class PlatformImpl {
    // Platform information
    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
    public static boolean isForgeLike() {
        return false;
    }
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    public static File getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    // Events
    public static void registerServerPostTickListener(Consumer<MinecraftServer> listener) {
        ServerTickEvents.END_SERVER_TICK.register(listener::accept);
    }
    public static void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(listener));
    }
    public static void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> listener.accept(handler.getPlayer()));
    }
    public static void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> listener.accept(handler.getPlayer()));
    }

    // Networking
    public static void sendToServer(FriendlyByteBuf message) {
        ClientPlayNetworking.send(ImmersiveMCFabric.C2S, message);
    }
    public static void sendToPlayer(ServerPlayer player, FriendlyByteBuf message) {
        ServerPlayNetworking.send(player, ImmersiveMCFabric.S2C, message);
    }
}
