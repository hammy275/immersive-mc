package com.hammy275.immersivemc.neoforge;

import com.hammy275.immersivemc.client.config.screen.ConfigScreen;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.network.Network;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientSetup {

    public static final List<KeyMapping> keyMappingsToRegister = new ArrayList<>();
    public static final List<Pair<ModelLayerLocation, Supplier<LayerDefinition>>> entityModelLayersToRegister = new ArrayList<>();
    public static final List<PiPRenderer<?>> pipRenderersToRegister = new ArrayList<>();

    public static void doClientSetup(IEventBus modBus) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (modContainer, screen) -> new ConfigScreen(screen));
        modBus.addListener((RegisterKeyMappingsEvent event) -> keyMappingsToRegister.forEach(event::register));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> entityModelLayersToRegister.forEach(pair -> event.registerLayerDefinition(pair.getFirst(), pair.getSecond())));
        NeoForge.EVENT_BUS.addListener((RenderLevelStageEvent.AfterOpaqueFeatures event) -> ClientRenderSubscriber.onWorldRender(event.getPoseStack()));
        modBus.addListener((RegisterPictureInPictureRenderersEvent event) -> pipRenderersToRegister.forEach(renderer -> renderer.register(event)));
        modBus.addListener((RegisterClientPayloadHandlersEvent event) -> event.register(BufferPacket.ID,
                (packet, ctx) -> Network.INSTANCE.doReceive(null, packet.buffer())));
    }

    public record PiPRenderer<S extends PictureInPictureRenderState>(Class<S> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> factory) {
        public void register(RegisterPictureInPictureRenderersEvent event) {
            event.register(stateClass, factory);
        }
    }
}
