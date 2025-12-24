package com.hammy275.immersivemc.common.obb;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.client.ClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.gizmos.CuboidGizmo;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.util.ARGB;

public class OBBClientUtil {

    public static void renderOBB(PoseStack stack, com.hammy275.immersivemc.api.common.hitbox.OBB obb, boolean forceRender,
                                 float red, float green, float blue, float alpha) {
        if ((Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES) || forceRender) &&
                obb != null) {
            Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
            // Use a new stack here, so we don't conflict with the stack.scale() for the item itself
            stack.pushPose();
            stack.translate(-renderInfo.position().x + obb.getCenter().x,
                    -renderInfo.position().y + obb.getCenter().y,
                    -renderInfo.position().z + obb.getCenter().z);
            rotateStackForOBB(stack, obb);
            ClientUtil.renderGizmo(new CuboidGizmo(obb.getUnderlyingAABB().move(obb.getCenter().scale(-1)), GizmoStyle.stroke(ARGB.colorFromFloat(alpha, red, green, blue)), false), stack);
            stack.popPose();
        }
    }

    public static void rotateStackForOBB(PoseStack stack, OBB obb) {
        stack.mulPose(obb.getRotation());
    }
}
