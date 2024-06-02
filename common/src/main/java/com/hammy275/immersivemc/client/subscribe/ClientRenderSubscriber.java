package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive_item.AbstractItemImmersive;
import com.hammy275.immersivemc.client.immersive_item.ItemImmersives;
import com.hammy275.immersivemc.client.model.Cube1x1;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.util.RGBA;
import com.hammy275.immersivemc.common.util.ShieldUtil;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ClientRenderSubscriber {

    public static final Cube1x1 cubeModel = new Cube1x1(Minecraft.getInstance().getEntityModels().bakeLayer(Cube1x1.LAYER_LOCATION));

    public static final List<ItemGuideRenderData> itemGuideRenderData = new ArrayList<>(128);

    public static void onWorldRender(PoseStack stack) {
        try {
            for (AbstractImmersive<? extends AbstractImmersiveInfo, ?> singleton : Immersives.IMMERSIVES) {
                renderInfos(singleton, stack);
            }
            if (VRPluginVerify.clientInVR()) {
                for (AbstractItemImmersive<?> singleton : ItemImmersives.ITEM_IMMERSIVES) {
                    singleton.renderAll(stack);
                }
            }
        } catch (ConcurrentModificationException ignored) {
            // Skip rendering if the list is modified mid-render
            // It's fine, since we were only going to read it anyway!!
        }

        // Draw shield hitbox(es)
        if (VRPluginVerify.clientInVR()) {
            for (InteractionHand iHand : InteractionHand.values()) {
                if (Minecraft.getInstance().player.getItemInHand(iHand).getUseAnimation() == UseAnim.BLOCK) {
                    IVRPlayer vrPlayer = Platform.isDevelopmentEnvironment() ?
                            VRPlugin.API.getVRPlayer(Minecraft.getInstance().player) :
                            VRPlugin.API.getRenderVRPlayer();
                    IVRData hand = vrPlayer.getController(iHand.ordinal());
                    AbstractImmersive.renderHitbox(stack, ShieldUtil.getShieldHitbox(Minecraft.getInstance().player, hand, iHand),
                            false, 1, 1, 1);
                }
            }
        }
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(); // Render out immersives

        // Render item guides separately so items behind item guides can be seen
        for (ItemGuideRenderData data : itemGuideRenderData) {
            renderItemGuide(data.stack, data.hitbox, data.alpha, data.isSelected, data.light);
        }
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        itemGuideRenderData.clear();
    }

    protected static <I extends AbstractImmersiveInfo> void renderInfos(AbstractImmersive<I, ?> singleton,
                                                                 PoseStack stack) {
        try {
            if (singleton.isVROnly() && !VRPluginVerify.clientInVR()) {
                return;
            }
            for (I info : singleton.getTrackedObjects()) {
                singleton.doRender(info, stack, VRPluginVerify.clientInVR());
            }
        } catch (ConcurrentModificationException ignored) {
            // Skip rendering if the list is modified mid-render
            // It's fine, since we were only going to read it anyway!!
        }
    }

    private static void renderItemGuide(PoseStack stack, BoundingBox hitbox, float alpha, boolean isSelected, int light) {
        if (hitbox != null && !Minecraft.getInstance().options.hideGui) {
            RGBA color = isSelected ? ActiveConfig.active().itemGuideSelectedColor : ActiveConfig.active().itemGuideColor;
            AABB aabb = hitbox.isAABB() ? hitbox.asAABB() : hitbox.asOBB().getUnderlyingAABB();
            float size = (float) aabb.getSize() * (isSelected ? (float) ActiveConfig.active().itemGuideSelectedSize : (float) ActiveConfig.active().itemGuideSize);
            if (ActiveConfig.active().placementGuideMode == PlacementGuideMode.CUBE) {
                Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
                Vec3 pos = aabb.getCenter();
                stack.pushPose();
                stack.translate(-renderInfo.getPosition().x + pos.x,
                        -renderInfo.getPosition().y + pos.y,
                        -renderInfo.getPosition().z + pos.z);
                MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                if (hitbox.isOBB()) {
                    OBBClientUtil.rotateStackForOBB(stack, hitbox.asOBB());
                }
                cubeModel.render(stack, buffer.getBuffer(RenderType.entityTranslucent(Cube1x1.textureLocation)),
                        color.redF(), color.greenF(), color.blueF(), color.alphaF(), size / 2, light);
                stack.popPose();
            } else if (ActiveConfig.active().placementGuideMode == PlacementGuideMode.OUTLINE) {
                if (hitbox.isAABB()) {
                    AbstractImmersive.renderHitbox(stack, AABB.ofSize(aabb.getCenter(), size, size, size), true,
                            color.redF(), color.greenF(), color.blueF(), color.alphaF());
                } else {
                    OBBClientUtil.renderOBB(stack, hitbox.asOBB(), false, color.redF(), color.greenF(), color.blueF(), color.alphaF());
                }
            }
        }
    }

    public record ItemGuideRenderData(PoseStack stack, BoundingBox hitbox, float alpha, boolean isSelected, int light) {}

}
