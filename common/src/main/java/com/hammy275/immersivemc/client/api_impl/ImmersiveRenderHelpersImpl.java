package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveRenderState;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.SwapTracker;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.obb.OBBRotList;
import com.hammy275.immersivemc.common.obb.RotType;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.CuboidGizmo;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.data.VRPose;

public class ImmersiveRenderHelpersImpl implements ImmersiveRenderHelpers {

    public static final ImmersiveRenderHelpers INSTANCE = new ImmersiveRenderHelpersImpl();

    @Override
    public void renderItemWithRenderState(ItemStack item, PoseStack stack, float size, boolean renderItemCounts, int light, ImmersiveRenderState renderState, boolean shouldRenderItemGuide, int hitboxIndex, @Nullable Float spinDegrees, @Nullable Direction facing, @Nullable Direction upDown) {
        BoundingBox hitbox = renderState.hitboxes().get(hitboxIndex);
        if (hitbox == null) return;
        boolean hovered = renderState.isSlotHovered(hitboxIndex) || SwapTracker.slotHovered(renderState, hitboxIndex);
        if (item == null || item.isEmpty()) {
            if (shouldRenderItemGuide) {
                renderItemGuide(stack, hitbox, hovered, light);
            }
        } else {
            long ticksExisted = renderState.ticksExisted();
            if (ticksExisted < ClientConstants.transitionTime) {
                // Adjust size based on transition
                size *= getTransitionMultiplier(renderState.ticksExisted());
            } else {
                // Adjust size based on if it's hovered
                size = hovered ? size * ClientConstants.sizeScaleForHover : size;
            }
            renderItem(item, stack, size, hitbox, renderItemCounts, light, spinDegrees, facing, upDown);
        }
    }

    @Override
    public void renderItem(ItemStack item, PoseStack stack, float size, BoundingBox hitbox, boolean renderItemCounts, int light) {
        this.renderItem(item, stack, size, hitbox, renderItemCounts, light, null, null, null);
    }

    @Override
    public void renderItem(ItemStack item, PoseStack stack, float size, BoundingBox hitbox, boolean renderItemCounts, int light, @Nullable Float spinDegrees, @Nullable Direction facing, @Nullable Direction upDown) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 pos = BoundingBox.getCenter(hitbox);
        if (item != null && item != ItemStack.EMPTY) {
            stack.pushPose();

            // Move the stack to be relative to the camera
            stack.translate(-renderInfo.position().x + pos.x,
                    -renderInfo.position().y + pos.y,
                    -renderInfo.position().z + pos.z);

            // Scale the item to be a good size
            stack.scale(size, size, size);

            Vec3 textPos = pos;

            // Rotate the item to face the player properly
            float degreesRotation = 0; // If North, we're already good
            if (spinDegrees != null) {
                degreesRotation = spinDegrees;
            } else if (facing == Direction.WEST) {
                degreesRotation = 90;
            } else if (facing == Direction.SOUTH) {
                degreesRotation = 180;
            } else if (facing == Direction.EAST) {
                degreesRotation = 270;
            }

            int upDownRot = 0; // If null, we're good
            if (upDown == Direction.UP) {
                upDownRot = 90;
                textPos = textPos.add(0, 0.15, 0);
            } else if (upDown == Direction.DOWN) {
                upDownRot = 270;
                textPos = textPos.add(0, -0.15, 0);
            } else if (facing == Direction.WEST) {
                textPos = textPos.add(-0.15, 0, 0);
            } else if (facing == Direction.SOUTH) {
                textPos = textPos.add(0, 0, 0.15);
            } else if (facing == Direction.EAST) {
                textPos = textPos.add(0.15, 0, 0);
            } else if (facing == Direction.NORTH) {
                textPos = textPos.add(0, 0, -0.15);
            } else if (facing == null) {
                faceTowardsPlayer(stack, BoundingBox.getCenter(hitbox));
                stack.mulPose(Axis.YP.rotationDegrees(180));
                Vec3 textMove;
                if (VRVerify.hasAPI && VRVerify.clientInVR()) {
                    VRPose textMovePose = VR.ClientAPI.getWorldRenderPose();
                    textMove = textMovePose.getHead().getDir();
                } else {
                    textMove = Minecraft.getInstance().player.getLookAngle();
                }
                textMove = textMove.multiply(-0.05, -0.05, -0.05);
                textPos = textPos.add(textMove);
            }

            if (facing != null) {
                stack.mulPose(Axis.YP.rotationDegrees(degreesRotation));
                stack.mulPose(Axis.XP.rotationDegrees(upDownRot));
            }

            ItemDisplayContext type = facing == null ? ItemDisplayContext.GROUND :
                    ItemDisplayContext.FIXED;

            ItemStackRenderState renderState = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForLiving(renderState, item, type, Minecraft.getInstance().player);
            renderState.submit(stack, Minecraft.getInstance().gameRenderer.getSubmitNodeStorage(), light, OverlayTexture.NO_OVERLAY, 0);

            stack.popPose();

            if (renderItemCounts && item.getCount() > 1) {
                this.renderText(Component.literal(String.valueOf(item.getCount())),
                        stack, textPos, light, facing == null ? 0.0025f : 0.01f);
            }
        }
        renderHitbox(stack, hitbox);
    }

    @Override
    public void renderItemGuide(PoseStack stack, BoundingBox hitbox, boolean isSelected, int light) {
        ClientRenderSubscriber.itemGuideRenderData.add(
                new ClientRenderSubscriber.ItemGuideRenderData(stack, hitbox, 0.2f, isSelected, light));
    }

    @Override
    public void renderHitbox(PoseStack stack, BoundingBox hitbox) {
        this.renderHitbox(stack, hitbox, false);
    }

    @Override
    public void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender) {
        this.renderHitbox(stack, hitbox, alwaysRender, 1, 1, 1);
    }

    @Override
    public void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender, float red, float green, float blue) {
        this.renderHitbox(stack, hitbox, alwaysRender, red, green, blue, 1);
    }

    @Override
    public void renderHitbox(PoseStack stack, BoundingBox hitbox, boolean alwaysRender, float red, float green, float blue, float alpha) {
        if ((Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES) || alwaysRender) &&
                hitbox != null) {
            if (hitbox.isAABB()) {
                stack.pushPose();
                ClientUtil.renderGizmo(new CuboidGizmo(hitbox.asAABB(), GizmoStyle.stroke(ARGB.colorFromFloat(alpha, red, green, blue)), false), stack);
                stack.popPose();
            } else {
                OBBClientUtil.renderOBB(stack, hitbox.asOBB(), alwaysRender, red, green, blue, alpha);
            }
        }
    }

    @Override
    public void renderText(Component text, PoseStack stack, Vec3 pos, int light, float textSize) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.mainCamera();
        textSize *= ActiveConfig.active().textScale;
        stack.pushPose();
        stack.translate(-renderInfo.position().x + pos.x,
                -renderInfo.position().y + pos.y,
                -renderInfo.position().z + pos.z);
        stack.mulPose(renderInfo.rotation());
        stack.scale(textSize, -textSize, textSize);
        Font font = Minecraft.getInstance().font;
        float size = -font.width(text) / 2f;
        font.drawInBatch(text, size, 0, 0xFFFFFFFF, false,
                stack.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL,
                0, light);
        stack.popPose();
    }

    @Override
    public void renderImage(PoseStack stack, Identifier imageLocation, Vec3 pos, float size, int light,
                            @Nullable Direction facing) {
        renderImage(stack, imageLocation, 0, 0, 1, 1, pos, size, light, facing);
    }

    @Override
    public void renderImage(PoseStack stack, Identifier imageLocation, float minImageU, float minImageV,
                            float maxImageU, float maxImageV, Vec3 pos, float size, int light,
                            @Nullable Direction facing) {
        renderImage(stack, imageLocation, minImageU, minImageV, maxImageU, maxImageV, pos, size, light, 0, facing);
    }

    @Override
    public void renderImage(PoseStack stack, Identifier imageLocation, float minImageU, float minImageV, float maxImageU, float maxImageV, Vec3 pos, float size, int light, float roll, @Nullable Direction facing) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.mainCamera();
        stack.pushPose();
        stack.translate(-renderInfo.position().x + pos.x,
                -renderInfo.position().y + pos.y,
                -renderInfo.position().z + pos.z);
        stack.scale(size, size, size);

        // If north, we're good to go
        if (facing == Direction.WEST) {
            stack.mulPose(Axis.YP.rotationDegrees(90));
        } else if (facing == Direction.SOUTH) {
            stack.mulPose(Axis.YP.rotationDegrees(180));
        } else if (facing == Direction.EAST) {
            stack.mulPose(Axis.YP.rotationDegrees(270));
        } else if (facing == null) {
            faceTowardsPlayer(stack, pos);
            stack.mulPose(Axis.YP.rotationDegrees(180));
        }
        stack.mulPose(Axis.ZN.rotationDegrees(roll));

        VertexConsumer consumer =
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypes.entityCutout(imageLocation));
        PoseStack.Pose pose = stack.last();

        consumer.addVertex(pose, -0.5f, -0.25f, 0)
                .setColor(0xFFFFFFFF)
                .setUv(minImageU, maxImageV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
        consumer.addVertex(pose, 0.5f, -0.25f, 0)
                .setColor(0xFFFFFFFF)
                .setUv(maxImageU, maxImageV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
        consumer.addVertex(pose, 0.5f, 0.75f, 0)
                .setColor(0xFFFFFFFF)
                .setUv(maxImageU, minImageV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
        consumer.addVertex(pose, -0.5f, 0.75f, 0)
                .setColor(0xFFFFFFFF)
                .setUv(minImageU, minImageV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);

        stack.popPose();
    }

    @Override
    public float getTransitionMultiplier(long ticksExisted) {
        return Math.min(1, ClientConstants.transitionMult * (ticksExisted + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true)));
    }

    @Override
    public float hoverScaleSizeMultiplier() {
        return ClientConstants.sizeScaleForHover;
    }

    private void faceTowardsPlayer(PoseStack stack, Vec3 renderPos) {
        if (VRVerify.clientInVR()) {
            Vec3 target = VR.ClientAPI.getWorldRenderPose().getHead().getPos();
            Vec3 ray = target.subtract(renderPos);
            Vec3 rayNoY = ray.multiply(1, 0, 1);
            OBBRotList rotList = OBBRotList.create()
                    .addRot(Math.atan2(ray.z, ray.x) + Math.PI / 2, RotType.YAW)
                    .addRot(-Math.atan2(ray.y, rayNoY.length()), RotType.PITCH);
            stack.mulPose(rotList.asQuaternion());
        } else {
            stack.mulPose(Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState.orientation);
        }
    }
}
