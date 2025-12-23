package com.hammy275.immersivemc.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public record ModelGuiRendererState(int x0, int x1, int y0, int y1, float scale, ScreenRectangle scissorArea,
                                    ScreenRectangle bounds, Model<?> model, Identifier texture, int rgb,
                                    Consumer<PoseStack> stackManipulator) implements PictureInPictureRenderState {

    public ModelGuiRendererState(double x0, double x1, double y0, double y1, float scale, ScreenRectangle scissorArea,
                                 Model<?> model, Identifier texture, int rgb, Consumer<PoseStack> stackManipulator) {
        this(
                (int) x0, (int) x1, (int) y0, (int) y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds((int) x0, (int) y0, (int) x1, (int) y1, scissorArea),
                model, texture, rgb, stackManipulator
        );
    }
}
