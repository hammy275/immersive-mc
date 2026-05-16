package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.PlatformCommon;
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

public class PlatformCommonImpl implements PlatformCommon {
    // Platform information
    @Override
    public boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }
    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }
    @Override
    public boolean isForgeLike() {
        return true;
    }
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.isLoaded(modId);
    }
    @Override
    public File getConfigFolder() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }

    // Events
    @Override
    public void registerServerPostTickListener(Consumer<MinecraftServer> listener) {
        TickEvent.ServerTickEvent.Post.BUS.addListener((TickEvent.ServerTickEvent.Post event) -> {
            listener.accept(event.server());
        });
    }
    @Override
    public void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        TickEvent.ServerTickEvent.Post.BUS.addListener((TickEvent.ServerTickEvent.Post event) -> {
            event.server().getPlayerList().getPlayers().forEach(listener);
        });
    }
    @Override
    public void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }
    @Override
    public void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }
    @Override
    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener) {
        RegisterCommandsEvent.BUS.addListener((RegisterCommandsEvent event) -> {
            listener.accept(event.getDispatcher());
        });
    }

    // Networking
    @Override
    public void sendToServer(RegistryFriendlyByteBuf message) {
        ImmersiveMCForge.NETWORK.send(new BufferPacket(message), PacketDistributor.SERVER.noArg());
    }
    @Override
    public void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf message) {
        ImmersiveMCForge.NETWORK.send(new BufferPacket(message), PacketDistributor.PLAYER.with(player));
    }

    // Misc.
    @Override
    public Fluid getFluid(BucketItem bucket) {
        return bucket.getFluid();
    }
}
