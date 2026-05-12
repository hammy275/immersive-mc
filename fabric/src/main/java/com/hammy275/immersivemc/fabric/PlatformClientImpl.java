package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.PlatformClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlatformClientImpl implements PlatformClient {
    // Events
    @Override
    public void registerOnClientJoinListener(Consumer<Minecraft> listener) {
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> listener.accept(client)));
    }
    @Override
    public void registerOnClientTickListener(Consumer<Minecraft> listener) {
        ClientTickEvents.END_CLIENT_TICK.register(listener::accept);
    }
    @Override
    public void registerOnClientDisconnectListener(Consumer<Player> listener) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> listener.accept(client.player));
    }

    // Registration
    @Override
    public void registerKeyMapping(KeyMapping keyMapping) {
        KeyBindingHelper.registerKeyBinding(keyMapping);
    }
    @Override
    public void registerEntityModelLayer(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
        EntityModelLayerRegistry.registerModelLayer(location, definition::get);
    }
    @Override
    public <S extends PictureInPictureRenderState> void registerPictureInPictureRenderer(Class<S> renderStateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> pipFactory) {
        SpecialGuiElementRegistry.register(ctx -> pipFactory.apply(ctx.vertexConsumers()));
    }

    // Rendering
    @Override
    public ScreenRectangle peekScissorStack(GuiGraphics guiGraphics) {
        return guiGraphics.scissorStack.peek();
    }
}
