package com.hammy275.immersivemc.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.util.function.Consumer;

public class PlatformImpl {
    // Platform information
    public static boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }
    public static boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }
    public static boolean isForgeLike() {
        return true;
    }
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
    public static File getConfigFolder() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }

    // Events
    public static void registerServerPostTickListener(Consumer<MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            listener.accept(event.getServer());
        });
    }
    public static void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            event.getServer().getPlayerList().getPlayers().forEach(listener);
        });
    }
    public static void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }
    public static void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }

    // Networking
    public static void sendToServer(RegistryFriendlyByteBuf message) {
        PacketDistributor.sendToServer(new BufferPacket(message));
    }
    public static void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf message) {
        PacketDistributor.sendToPlayer(player, new BufferPacket(message));
    }
}
