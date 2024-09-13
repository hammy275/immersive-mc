package com.hammy275.immersivemc.client.api_impl;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.HitboxInfo;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.obb.OBBRotList;
import com.hammy275.immersivemc.common.obb.RotType;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.mixin.DragonFireballRendererMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ImmersiveRenderHelpersImpl implements ImmersiveRenderHelpers {

    public static final ImmersiveRenderHelpers INSTANCE = new ImmersiveRenderHelpersImpl();

    @Override
    public void renderItemWithInfo(ItemStack item, PoseStack stack, float size, boolean renderItemCounts, int light, ImmersiveInfo info, boolean shouldRenderItemGuide, int hitboxIndex, @Nullable Float spinDegrees, @Nullable Direction facing, @Nullable Direction upDown) {
        HitboxInfo hitbox = info.getAllHitboxes().get(hitboxIndex);
        boolean hovered = info.getSlotHovered(0) == hitboxIndex || info.getSlotHovered(1) == hitboxIndex;
        if (item == null || item.isEmpty()) {
            if (shouldRenderItemGuide) {
                renderItemGuide(stack, hitbox.getHitbox(), hovered, light);
            }
        } else {
            long ticksExisted = info.getTicksExisted();
            if (ticksExisted < ClientConstants.transitionTime) {
                // Adjust size based on transition
                size *= getTransitionMultiplier(info.getTicksExisted());
            } else {
                // Adjust size based on if it's hovered
                size = hovered ? size * ClientConstants.sizeScaleForHover : size;
            }
            BoundingBox bbox = hitbox.getHitbox();
            renderItem(item, stack, size, bbox, renderItemCounts, light, spinDegrees, facing, upDown);
        }
    }

    @Override
    public void renderItem(ItemStack item, PoseStack stack, float size, BoundingBox hitbox, boolean renderItemCounts, int light) {
        this.renderItem(item, stack, size, hitbox, renderItemCounts, light, null, null, null);
    }

    @Override
    public void renderItem(ItemStack item, PoseStack stack, float size, BoundingBox hitbox, boolean renderItemCounts, int light, @Nullable Float spinDegrees, @Nullable Direction facing, @Nullable Direction upDown) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 pos = BoundingBox.getCenter(hitbox);
        if (item != null && item != ItemStack.EMPTY) {
            stack.pushPose();

            // Move the stack to be relative to the camera
            stack.translate(-renderInfo.getPosition().x + pos.x,
                    -renderInfo.getPosition().y + pos.y,
                    -renderInfo.getPosition().z + pos.z);

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
                if (VRPluginVerify.hasAPI && VRPluginVerify.clientInVR()) {
                    IVRPlayer textMovePlayer = Platform.isDevelopmentEnvironment() ?
                            VRPlugin.API.getVRPlayer(Minecraft.getInstance().player) :
                            VRPlugin.API.getRenderVRPlayer();
                    textMove = textMovePlayer.getHMD().getLookAngle();
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

            Minecraft.getInstance().getItemRenderer().renderStatic(item, type,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    stack, Minecraft.getInstance().renderBuffers().bufferSource(), Minecraft.getInstance().level, 0);

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
        if ((Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() || alwaysRender) &&
                hitbox != null) {
            if (hitbox.isAABB()) {
                Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
                // Use a new stack here, so we don't conflict with the stack.scale() for the item itself
                stack.pushPose();
                stack.translate(-renderInfo.getPosition().x,
                        -renderInfo.getPosition().y,
                        -renderInfo.getPosition().z);
                MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                LevelRenderer.renderLineBox(stack, buffer.getBuffer(RenderType.LINES),
                        hitbox.asAABB(),
                        red, green, blue, alpha);
                stack.popPose();
            } else {
                OBBClientUtil.renderOBB(stack, hitbox.asOBB(), alwaysRender, red, green, blue, alpha);
            }
        }
    }

    @Override
    public void renderText(Component text, PoseStack stack, Vec3 pos, int light, float textSize) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.pushPose();
        stack.translate(-renderInfo.getPosition().x + pos.x,
                -renderInfo.getPosition().y + pos.y,
                -renderInfo.getPosition().z + pos.z);
        stack.mulPose(renderInfo.rotation());
        stack.scale(-textSize, -textSize, -textSize);
        Font font = Minecraft.getInstance().font;
        float size = -font.width(text) / 2f;
        font.drawInBatch(text, size, 0, 0xFFFFFFFF, false,
                stack.last().pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL,
                0, light);
        stack.popPose();
    }

    @Override
    public void renderImage(PoseStack stack, ResourceLocation imageLocation, Vec3 pos, float size, int light,
                            @Nullable Direction facing) {
        Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
        stack.pushPose();
        stack.translate(-renderInfo.getPosition().x + pos.x,
                -renderInfo.getPosition().y + pos.y,
                -renderInfo.getPosition().z + pos.z);
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

        VertexConsumer vertexConsumer =
                Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.entityCutoutNoCull(imageLocation));
        PoseStack.Pose pose = stack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        DragonFireballRendererMixin.doVertex(vertexConsumer, matrix4f, matrix3f, light, 0f, 0, 0, 1);
        DragonFireballRendererMixin.doVertex(vertexConsumer, matrix4f, matrix3f, light, 1f, 0, 1, 1);
        DragonFireballRendererMixin.doVertex(vertexConsumer, matrix4f, matrix3f, light, 1f, 1, 1, 0);
        DragonFireballRendererMixin.doVertex(vertexConsumer, matrix4f, matrix3f, light, 0f, 1, 0, 0);

        stack.popPose();
    }

    @Override
    public float getTransitionMultiplier(long ticksExisted) {
        return Math.min(1, ClientConstants.transitionMult * (ticksExisted + Minecraft.getInstance().getFrameTime()));
    }

    @Override
    public float hoverScaleSizeMultiplier() {
        return ClientConstants.sizeScaleForHover;
    }

    private void faceTowardsPlayer(PoseStack stack, Vec3 renderPos) {
        if (VRPluginVerify.clientInVR()) {
            Vec3 target = Platform.isDevelopmentEnvironment() ?
                    VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getHMD().position() :
                    VRPlugin.API.getRenderVRPlayer().getHMD().position();
            Vec3 ray = target.subtract(renderPos);
            Vec3 rayNoY = ray.multiply(1, 0, 1);
            OBBRotList rotList = OBBRotList.create()
                    .addRot(Math.atan2(ray.z, ray.x) + Math.PI / 2, RotType.YAW)
                    .addRot(-Math.atan2(ray.y, rayNoY.length()), RotType.PITCH);
            stack.mulPose(rotList.asQuaternion());
        } else {
            stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        }
    }
}
