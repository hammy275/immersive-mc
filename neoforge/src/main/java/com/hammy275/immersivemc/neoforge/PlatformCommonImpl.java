package com.hammy275.immersivemc.neoforge;

import com.hammy275.immersivemc.PlatformCommon;
import com.hammy275.immersivemc.mixin.BucketItemAccessor;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.util.function.Consumer;

public class PlatformCommonImpl implements PlatformCommon {
    // Platform information
    @Override
    public boolean isClient() {
        return FMLEnvironment.getDist() == Dist.CLIENT;
    }
    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.isProduction();
    }
    @Override
    public boolean isForgeLike() {
        return true;
    }
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
    @Override
    public File getConfigFolder() {
        return FMLPaths.CONFIGDIR.get().toFile();
    }

    // Events
    @Override
    public void registerServerPostTickListener(Consumer<MinecraftServer> listener) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            listener.accept(event.getServer());
        });
    }
    @Override
    public void registerServerPlayerPostTickListener(Consumer<ServerPlayer> listener) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            event.getServer().getPlayerList().getPlayers().forEach(listener);
        });
    }
    @Override
    public void registerServerPlayerJoinListener(Consumer<ServerPlayer> listener) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }
    @Override
    public void registerServerPlayerLeaveListener(Consumer<ServerPlayer> listener) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                listener.accept(sp);
            }
        });
    }
    @Override
    public void registerCommands(Consumer<CommandDispatcher<CommandSourceStack>> listener) {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            listener.accept(event.getDispatcher());
        });
    }

    // Networking
    @Override
    public void sendToServer(RegistryFriendlyByteBuf message) {
        ClientPacketDistributor.sendToServer(new BufferPacket(message));
    }
    @Override
    public void sendToPlayer(ServerPlayer player, RegistryFriendlyByteBuf message) {
        PacketDistributor.sendToPlayer(player, new BufferPacket(message));
    }

    // Misc.
    @Override
    public Fluid getFluid(BucketItem bucket) {
        return ((BucketItemAccessor) bucket).immersiveMC$getFluid();
    }
}
