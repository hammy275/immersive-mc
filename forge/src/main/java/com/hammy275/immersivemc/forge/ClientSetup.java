package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientSetup {

    public static final List<KeyMapping> keyMappingsToRegister = new ArrayList<>();
    public static final List<Pair<ModelLayerLocation, Supplier<LayerDefinition>>> entityModelLayersToRegister = new ArrayList<>();

    public static void doClientSetup() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
        FMLJavaModLoadingContext.get().getModBusGroup().register(MethodHandles.lookup(), ClientSetup.class);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        keyMappingsToRegister.forEach(event::register);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        entityModelLayersToRegister.forEach(pair -> event.registerLayerDefinition(pair.getFirst(), pair.getSecond()));
    }

    @SubscribeEvent
    public static void levelRenderAfterEntities(TODO) {
        ClientRenderSubscriber.onWorldRender(TODO);
    }
}
