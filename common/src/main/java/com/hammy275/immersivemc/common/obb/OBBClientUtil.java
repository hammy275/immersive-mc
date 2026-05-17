package com.hammy275.immersivemc.common.obb;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.client.ClientUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class OBBClientUtil {

    public static void renderOBB(PoseStack stack, com.hammy275.immersivemc.api.common.hitbox.OBB obb, boolean forceRender,
                                 float red, float green, float blue, float alpha) {
        if ((Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES) || forceRender) &&
                obb != null) {
            int color = ARGB.colorFromFloat(alpha, red, green, blue);
            DrawableGizmoPrimitives gizmoPrimitives = new DrawableGizmoPrimitives();
            List<Vec3> vertices = obb.getVertices();
            for (int i = 0; i <= 3; i++) {
                gizmoPrimitives.addLine(vertices.get(i), vertices.get((i + 1) % 4), color, 2.5f);
                gizmoPrimitives.addLine(vertices.get(i + 4), vertices.get((i + 1) % 4 + 4), color, 2.5f);
                gizmoPrimitives.addLine(vertices.get(i), vertices.get(i + 4), color, 2.5f);
            }
            ClientUtil.renderGizmoPrimitives(gizmoPrimitives, stack);
        }
    }

    public static void rotateStackForOBB(PoseStack stack, OBB obb) {
        stack.mulPose(obb.getRotation());
    }
}
