package com.hammy275.immersivemc.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

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
                listener.accept(ServerLifecycleHooks.getCurrentServer());
            }
        });
    }
    public static void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(listener);
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
    public static void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener) {
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            listener.accept(event.getDispatcher());
        });
    }

    // Networking
    public static void sendToServer(FriendlyByteBuf message) {
        ImmersiveMCForge.NETWORK.sendToServer(new BufferPacket(message));
    }
    public static void sendToPlayer(ServerPlayer player, FriendlyByteBuf message) {
        ImmersiveMCForge.NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new BufferPacket(message));
    }

    // Misc.
    public static Fluid getFluid(BucketItem bucket) {
        return bucket.getFluid();
    }
}
