package com.hammy275.immersivemc.client.model;

import com.hammy275.immersivemc.ImmersiveMC;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

public class CustomGuiRenderer extends PictureInPictureRenderer<CustomGuiRendererState> {
    public CustomGuiRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<CustomGuiRendererState> getRenderStateClass() {
        return CustomGuiRendererState.class;
    }

    @Override
    protected void renderToTexture(CustomGuiRendererState renderState, PoseStack poseStack) {
        poseStack.pushPose();
        renderState.renderer().accept(poseStack, bufferSource);
        poseStack.popPose();
    }

    @Override
    protected String getTextureLabel() {
        return ImmersiveMC.MOD_ID + "_modelguirenderer";
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2f;
    }
}
