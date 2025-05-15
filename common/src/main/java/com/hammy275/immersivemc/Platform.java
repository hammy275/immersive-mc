package com.hammy275.immersivemc;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.io.File;
import java.util.function.Consumer;

public class Platform {
    // Platform information
    @ExpectPlatform
    public static boolean isClient() {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static boolean isDevelopmentEnvironment() {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static boolean isForgeLike() {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static File getConfigFolder() {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }

    // Events
    @ExpectPlatform
    public static void registerServerPostTickListener(Consumer<MinecraftServer> listener) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }

    // Networking
    @ExpectPlatform
    public static void sendToServer(RegistryFriendlyByteBuf message) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf message) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }

    // Misc.
    @ExpectPlatform
    public static Fluid getFluid(BucketItem bucket) {
        return Fluids.EMPTY; // Return some default fluid so callers don't get odd warnings
    }
}
