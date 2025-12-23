package com.hammy275.immersivemc.client.model;

import com.hammy275.immersivemc.ImmersiveMC;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class ModelGuiRenderer extends PictureInPictureRenderer<ModelGuiRendererState> {
    public ModelGuiRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<ModelGuiRendererState> getRenderStateClass() {
        return ModelGuiRendererState.class;
    }

    @Override
    protected void renderToTexture(ModelGuiRendererState renderState, PoseStack poseStack) {
        poseStack.pushPose();
        renderState.stackManipulator().accept(poseStack);
        renderState.model().renderToBuffer(poseStack, bufferSource.getBuffer(RenderTypes.entityCutout(renderState.texture())),
                15728880, OverlayTexture.NO_OVERLAY, renderState.rgb());
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
