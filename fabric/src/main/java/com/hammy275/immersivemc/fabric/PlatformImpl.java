package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.mixin.BucketItemAccessor;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

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
    public static void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener) {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> 
                listener.accept(dispatcher)));
    }

    // Networking
    public static void sendToServer(RegistryFriendlyByteBuf message) {
        ClientPlayNetworking.send(new BufferPacket(message));
    }
    public static void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf message) {
        ServerPlayNetworking.send(player, new BufferPacket(message));
    }

    // Misc.
    public static Fluid getFluid(BucketItem bucket) {
        return ((BucketItemAccessor) bucket).immersiveMC$getFluid();
    }
}
