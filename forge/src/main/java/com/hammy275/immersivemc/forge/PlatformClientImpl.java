package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.PlatformClient;
import com.mojang.datafixers.util.Pair;
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
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlatformClientImpl implements PlatformClient {
    // Events
    @Override
    public void registerOnClientJoinListener(Consumer<Minecraft> listener) {
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            listener.accept(Minecraft.getInstance());
        });
    }
    @Override
    public void registerOnClientTickListener(Consumer<Minecraft> listener) {
        TickEvent.ClientTickEvent.Post.BUS.addListener((TickEvent.ClientTickEvent.Post event) -> {
            listener.accept(Minecraft.getInstance());
        });
    }
    @Override
    public void registerOnClientDisconnectListener(Consumer<Player> listener) {
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            if (event.getPlayer() != null) {  // Fires with null player on login. Don't pass these fires on to listeners.
                listener.accept(event.getPlayer());
            }
        });
    }

    // Registration
    @Override
    public void registerKeyMapping(KeyMapping keyMapping) {
        ClientSetup.keyMappingsToRegister.add(keyMapping);
    }
    @Override
    public void registerEntityModelLayer(ModelLayerLocation location, Supplier<LayerDefinition> definition) {
        ClientSetup.entityModelLayersToRegister.add(new Pair<>(location, definition));
    }
    @Override
    public <S extends PictureInPictureRenderState> void registerPictureInPictureRenderer(Class<S> renderStateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> pipFactory) {
        ClientSetup.pipRenderersToRegister.add(new ClientSetup.PiPRenderer<>(renderStateClass, pipFactory));
    }

    // Rendering
    @Override
    public ScreenRectangle peekScissorStack(GuiGraphics guiGraphics) {
        return guiGraphics.getScissorStack().peek();
    }
}
