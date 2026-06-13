package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.function.BiConsumer;

/**
 * A functional interface for {@link BuiltBlockBasedImmersive}s for providing additional rendering functionality.
 * @param <ER> The extra data passed to rendering. This is the second class passed to
 *             {@link BlockBasedImmersiveBuilder#copy(BlockBasedImmersiveHandler, Class, Class, BiConsumer)}.
 */
@FunctionalInterface
public interface ExtraRenderer<ER> {
    /**
     * Called when rendering after hitboxes have been rendered.
     * @param renderState The render state that was rendered.
     * @param stack The pose stack being rendered with.
     * @param helpers Some helper functions for rendering.
     * @param partialTick The fraction of time between the last tick and the current tick.
     * @param light The light value for the Immersive.
     */
    void render(BuiltImmersiveRenderState<ER> renderState, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick, int light);
}
