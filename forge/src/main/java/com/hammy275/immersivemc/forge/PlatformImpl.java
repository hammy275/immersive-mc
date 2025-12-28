package com.hammy275.immersivemc.forge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
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
        TickEvent.ServerTickEvent.Post.BUS.addListener((TickEvent.ServerTickEvent.Post event) -> {
            listener.accept(event.server());
        });
    }
    public static void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        TickEvent.ServerTickEvent.Post.BUS.addListener((TickEvent.ServerTickEvent.Post event) -> {
            event.server().getPlayerList().getPlayers().forEach(listener);
        });
    }
    public static void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }
    public static void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }
    public static void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener) {
        RegisterCommandsEvent.BUS.addListener((RegisterCommandsEvent event) -> {
            listener.accept(event.getDispatcher());
        });
    }

    // Networking
    public static void sendToServer(RegistryFriendlyByteBuf message) {
        ImmersiveMCForge.NETWORK.send(new BufferPacket(message), PacketDistributor.SERVER.noArg());
    }
    public static void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf message) {
        ImmersiveMCForge.NETWORK.send(new BufferPacket(message), PacketDistributor.PLAYER.with(player));
    }

    // Misc.
    public static Fluid getFluid(BucketItem bucket) {
        return bucket.getFluid();
    }
}
