package com.hammy275.immersivemc.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.BiConsumer;

public record CustomGuiRendererState(int x0, int x1, int y0, int y1, float scale, ScreenRectangle scissorArea,
                                     ScreenRectangle bounds,
                                     BiConsumer<PoseStack, MultiBufferSource.BufferSource> renderer) implements PictureInPictureRenderState {

    public CustomGuiRendererState(double x0, double x1, double y0, double y1, float scale, ScreenRectangle scissorArea,
                                  BiConsumer<PoseStack, MultiBufferSource.BufferSource> renderer) {
        this(
                (int) x0, (int) x1, (int) y0, (int) y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds((int) x0, (int) y0, (int) x1, (int) y1, scissorArea),
                renderer
        );
    }
}
