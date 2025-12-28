package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientSetup {

    public static final List<KeyMapping> keyMappingsToRegister = new ArrayList<>();
    public static final List<Pair<ModelLayerLocation, Supplier<LayerDefinition>>> entityModelLayersToRegister = new ArrayList<>();
    public static final List<PiPRenderer<?>> pipRenderersToRegister = new ArrayList<>();

    public static void doClientSetup() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen)));
        RegisterKeyMappingsEvent.BUS.addListener((RegisterKeyMappingsEvent event) -> keyMappingsToRegister.forEach(event::register));
        EntityRenderersEvent.RegisterLayerDefinitions.BUS.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                entityModelLayersToRegister.forEach(pair -> event.registerLayerDefinition(pair.getFirst(), pair.getSecond())));
        RegisterPictureInPictureRendererEvent.BUS.addListener((RegisterPictureInPictureRendererEvent event) -> pipRenderersToRegister.forEach(renderer -> renderer.register(event)));
    }

    public record PiPRenderer<S extends PictureInPictureRenderState>(Class<S> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> factory) {
        public void register(RegisterPictureInPictureRendererEvent event) {
            event.register(factory.apply(event.getBufferSource()));
        }
    }
}
