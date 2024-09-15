package com.hammy275.immersivemc.neoforge;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlatformClientImpl {
    // Events
    public static void registerOnClientJoinListener(Consumer<Minecraft> listener) {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            listener.accept(Minecraft.getInstance());
        });
    }
    public static void registerOnClientTickListener(Consumer<Minecraft> listener) {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            listener.accept(Minecraft.getInstance());
        });
    }
    public static void registerOnClientDisconnectListener(Consumer<Player> listener) {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
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
