package com.hammy275.immersivemc;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

import java.io.File;
import java.util.function.Consumer;

public interface PlatformCommon {
    // Platform information
    boolean isClient();
    boolean isDevelopmentEnvironment();
    boolean isForgeLike();
    boolean isModLoaded(String modId);
    File getConfigFolder();

    // Events
    void registerServerPostTickListener(Consumer<MinecraftServer> listener);
    void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener);
    void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener);
    void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener);
    void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener);

    // Networking
    void sendToServer(RegistryFriendlyByteBuf message);
    void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf message);

    // Misc.
    Fluid getFluid(BucketItem bucket);
}
