package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientSetup {

    public static final List<KeyMapping> keyMappingsToRegister = new ArrayList<>();
    public static final List<Pair<ModelLayerLocation, Supplier<LayerDefinition>>> entityModelLayersToRegister = new ArrayList<>();

    public static void doClientSetup() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen)));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> keyMappingsToRegister.forEach(ClientRegistry::registerKeyBinding)));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> entityModelLayersToRegister.forEach(pair -> event.registerLayerDefinition(pair.getFirst(), pair.getSecond())));
    }
}
