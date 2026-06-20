package com.hammy275.immersivemc.client.model;

import com.hammy275.immersivemc.ImmersiveMC;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

public class CustomGuiRenderer extends PictureInPictureRenderer<CustomGuiRendererState> {

    @Override
    public Class<CustomGuiRendererState> getRenderStateClass() {
        return CustomGuiRendererState.class;
    }

    @Override
    protected void renderToTexture(CustomGuiRendererState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        renderState.renderer().accept(poseStack, submitNodeCollector);
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
