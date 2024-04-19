package com.hammy275.immersivemc.common.obb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class OBBClientUtil {

    public static void renderOBB(PoseStack stack, OBB obb, boolean forceRender,
                                 float red, float green, float blue, float alpha) {
        if ((Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() || forceRender) &&
                obb != null) {
            Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
            // Use a new stack here, so we don't conflict with the stack.scale() for the item itself
            stack.pushPose();
            stack.translate(-renderInfo.getPosition().x + obb.center.x,
                    -renderInfo.getPosition().y + obb.center.y,
                    -renderInfo.getPosition().z + obb.center.z);
            rotateStackForOBB(stack, obb);
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            LevelRenderer.renderLineBox(stack, buffer.getBuffer(RenderType.LINES),
                    obb.aabb.move(obb.center.scale(-1)),
                    red, green, blue, alpha);
            stack.popPose();
        }
    }

    public static void rotateStackForOBB(PoseStack stack, OBB obb) {
        // Note: Roll likely doesn't currently work lol
        stack.mulPose(Vector3f.ZN.rotation((float) obb.roll));
        stack.mulPose(Vector3f.YN.rotation((float) obb.yaw));
        stack.mulPose(Vector3f.XN.rotation((float) obb.pitch));
    }
}
