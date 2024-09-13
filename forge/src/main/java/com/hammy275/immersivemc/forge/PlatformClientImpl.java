package com.hammy275.immersivemc.forge;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlatformClientImpl {
    // Events
    public static void registerOnClientJoinListener(Consumer<Minecraft> listener) {
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggedInEvent event) -> {
            listener.accept(Minecraft.getInstance());
        });
    }
    public static void registerOnClientTickListener(Consumer<Minecraft> listener) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                listener.accept(Minecraft.getInstance());
            }
        });
    }
    public static void registerOnClientDisconnectListener(Consumer<Player> listener) {
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggedOutEvent event) -> {
            listener.accept(event.getPlayer());
        });
    }

    // Registration
    public static void registerKeyMapping(KeyMapping keyMapping) {
        ClientSetup.keyMappingsToRegister.add(keyMapping);
    }
    public static void registerEntityModelLayer(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
        ClientSetup.entityModelLayersToRegister.add(new Pair<>(location, definition));
    }
}
