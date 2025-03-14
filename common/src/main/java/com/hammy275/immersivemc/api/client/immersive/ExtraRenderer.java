package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.mojang.blaze3d.vertex.PoseStack;

@FunctionalInterface
public interface ExtraRenderer<E> {
    /**
     * Called when rendering after hitboxes have been rendered.
     * @param info The info to render.
     * @param stack The pose stack being rendered with.
     * @param helpers Some helper functions for rendering.
     * @param partialTick The fraction of time between the last tick and the current tick.
     * @param light The light value for the Immersive.
     */
    void render(BuiltImmersiveInfo<E> info, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick, int light);
}
