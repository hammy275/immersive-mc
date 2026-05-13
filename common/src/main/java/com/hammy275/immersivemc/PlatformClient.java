package com.hammy275.immersivemc;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PlatformClient {
    // Events
    void registerOnClientJoinListener(Consumer<Minecraft> listener);
    void registerOnClientTickListener(Consumer<Minecraft> listener);
    void registerOnClientDisconnectListener(Consumer<Player> listener);

    // Registration
    void registerKeyMapping(KeyMapping keyMapping);
    void registerEntityModelLayer(ModelLayerLocation location, Supplier<LayerDefinition> definition);
    <S extends PictureInPictureRenderState> void registerPictureInPictureRenderer(Class<S> renderStateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> pipFactory);

    // Rendering
    ScreenRectangle peekScissorStack(GuiGraphicsExtractor guiGraphicsExtractor);
}
