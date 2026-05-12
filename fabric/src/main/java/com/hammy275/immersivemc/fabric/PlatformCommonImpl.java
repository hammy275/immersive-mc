package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.PlatformCommon;
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

public class PlatformCommonImpl implements PlatformCommon {
    // Platform information
    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
    @Override
    public boolean isForgeLike() {
        return false;
    }
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    @Override
    public File getConfigFolder() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    // Events
    @Override
    public void registerServerPostTickListener(Consumer<MinecraftServer> listener) {
        ServerTickEvents.END_SERVER_TICK.register(listener::accept);
    }
    @Override
    public void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(listener));
    }
    @Override
    public void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> listener.accept(handler.getPlayer()));
    }
    @Override
    public void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> listener.accept(handler.getPlayer()));
    }
    @Override
    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener) {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> 
                listener.accept(dispatcher)));
    }

    // Networking
    @Override
    public void sendToServer(RegistryFriendlyByteBuf message) {
        ClientPlayNetworking.send(new BufferPacket(message));
    }
    @Override
    public void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf message) {
        ServerPlayNetworking.send(player, new BufferPacket(message));
    }

    // Misc.
    @Override
    public Fluid getFluid(BucketItem bucket) {
        return ((BucketItemAccessor) bucket).immersiveMC$getFluid();
    }
}
