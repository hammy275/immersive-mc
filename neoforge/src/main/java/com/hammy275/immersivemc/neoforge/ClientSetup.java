package com.hammy275.immersivemc.neoforge;

import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientSetup {

    public static final List<KeyMapping> keyMappingsToRegister = new ArrayList<>();
    public static final List<Pair<ModelLayerLocation, Supplier<LayerDefinition>>> entityModelLayersToRegister = new ArrayList<>();

    public static void doClientSetup(IEventBus modBus) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
        modBus.addListener((RegisterKeyMappingsEvent event) -> keyMappingsToRegister.forEach(event::register));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> entityModelLayersToRegister.forEach(pair -> event.registerLayerDefinition(pair.getFirst(), pair.getSecond())));
    }
}
