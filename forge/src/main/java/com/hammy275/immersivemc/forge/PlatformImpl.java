package com.hammy275.immersivemc.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

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
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                listener.accept(event.getServer());
            }
        });
    }
    public static void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                event.getServer().getPlayerList().getPlayers().forEach(listener);
            }
        });
    }
    public static void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }
    public static void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }

    // Networking
    public static void sendToServer(FriendlyByteBuf message) {
        ImmersiveMCForge.NETWORK.send(new BufferPacket(message), PacketDistributor.SERVER.noArg());
    }
    public static void sendToPlayer(ServerPlayer player, FriendlyByteBuf message) {
        ImmersiveMCForge.NETWORK.send(new BufferPacket(message), PacketDistributor.PLAYER.with(player));
    }
}
