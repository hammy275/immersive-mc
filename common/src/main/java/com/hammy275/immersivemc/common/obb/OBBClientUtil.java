package com.hammy275.immersivemc.common.obb;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class OBBClientUtil {

    public static void renderOBB(PoseStack stack, com.hammy275.immersivemc.api.common.hitbox.OBB obb, boolean forceRender,
                                 float red, float green, float blue, float alpha) {
        if ((Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() || forceRender) &&
                obb != null) {
            Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
            // Use a new stack here, so we don't conflict with the stack.scale() for the item itself
            stack.pushPose();
            stack.translate(-renderInfo.getPosition().x + obb.getCenter().x,
                    -renderInfo.getPosition().y + obb.getCenter().y,
                    -renderInfo.getPosition().z + obb.getCenter().z);
            rotateStackForOBB(stack, obb);
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            LevelRenderer.renderLineBox(stack, buffer.getBuffer(RenderType.LINES),
                    obb.getUnderlyingAABB().move(obb.getCenter().scale(-1)),
                    red, green, blue, alpha);
            stack.popPose();
        }
    }

    public static void rotateStackForOBB(PoseStack stack, OBB obb) {
        stack.mulPose(obb.getRotation());
    }
}
