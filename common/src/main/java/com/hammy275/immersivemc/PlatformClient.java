package com.hammy275.immersivemc;

import dev.architectury.injectables.annotations.ExpectPlatform;
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

public class PlatformClient {
    // Events
    @ExpectPlatform
    public static void registerOnClientJoinListener(Consumer<Minecraft> listener) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static void registerOnClientTickListener(Consumer<Minecraft> listener) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static void registerOnClientDisconnectListener(Consumer<Player> listener) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }

    // Registration
    @ExpectPlatform
    public static void registerKeyMapping(KeyMapping keyMapping) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static void registerEntityModelLayer(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
    @ExpectPlatform
    public static <S extends PictureInPictureRenderState> void registerPictureInPictureRenderer(Class<S> renderStateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> pipFactory) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }

    // Rendering
    @ExpectPlatform
    public static ScreenRectangle peekScissorStack(GuiGraphics guiGraphics) {
        throw new RuntimeException("@ExpectPlatform should have replaced this");
    }
}
