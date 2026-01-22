package com.hammy275.immersivemc.common.obb;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class OBBClientUtil {

    public static void renderOBB(PoseStack stack, com.hammy275.immersivemc.api.common.hitbox.OBB obb, boolean forceRender,
                                 float red, float green, float blue, float alpha) {
        if ((Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES) || forceRender) &&
                obb != null) {
            Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
            stack.pushPose();
            stack.translate(-renderInfo.position().x + obb.getCenter().x,
                    -renderInfo.position().y + obb.getCenter().y,
                    -renderInfo.position().z + obb.getCenter().z);
            rotateStackForOBB(stack, obb);
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            net.minecraft.client.renderer.ShapeRenderer.renderLineBox(stack.last(), buffer.getBuffer(RenderType.LINES),
                    obb.getUnderlyingAABB().move(obb.getCenter().scale(-1)),
                    red, green, blue, alpha);
            stack.popPose();
        }
    }

    public static void rotateStackForOBB(PoseStack stack, OBB obb) {
        stack.mulPose(obb.getRotation());
    }
}
