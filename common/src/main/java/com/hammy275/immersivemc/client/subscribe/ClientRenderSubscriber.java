package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.AbstractPlayerAttachmentInfo;
import com.hammy275.immersivemc.client.immersive_item.AbstractHandImmersive;
import com.hammy275.immersivemc.client.immersive_item.HandImmersives;
import com.hammy275.immersivemc.client.model.Cube1x1;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.ItemGuideColorData;
import com.hammy275.immersivemc.common.config.PlacementGuideMode;
import com.hammy275.immersivemc.common.obb.OBBClientUtil;
import com.hammy275.immersivemc.common.util.RGBA;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ClientRenderSubscriber {

    public static final Cube1x1 cubeModel = new Cube1x1(Minecraft.getInstance().getEntityModels().bakeLayer(Cube1x1.LAYER_LOCATION));

    public static final List<ItemGuideRenderData> itemGuideRenderData = new ArrayList<>(128);

    private static long lastMillis;
    private static RGBA itemGuideColor;
    private static float cycleProgress;
    private static RGBA itemGuideSelectedColor;
    private static float cycleProgressSelected;
    private static RGBA rangedGrabColor;
    private static float cycleProgressRangedGrab;

    public static void onWorldRender(PoseStack stack) {
        setRenderColors();
        try {
            for (Immersive<?, ?> singleton : Immersives.IMMERSIVES) {
                renderInfos(singleton, stack);
            }
            for (AbstractPlayerAttachmentImmersive<? extends AbstractPlayerAttachmentInfo, ?> singleton : Immersives.IMMERSIVE_ATTACHMENTS) {
                renderInfos(singleton, stack);
            }
            if (VRVerify.clientInVR()) {
                for (AbstractHandImmersive<?> singleton : HandImmersives.HAND_IMMERSIVES) {
                    singleton.renderAll(stack);
                }
            }
        } catch (ConcurrentModificationException ignored) {
            // Skip rendering if the list is modified mid-render
            // It's fine, since we were only going to read it anyway!!
        }

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(); // Render out immersives

        // Render item guides separately so items behind item guides can be seen
        for (ItemGuideRenderData data : itemGuideRenderData) {
            renderItemGuide(data.stack, data.hitbox, data.alpha, data.isSelected, data.light);
        }
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        itemGuideRenderData.clear();
    }

    public static void setRenderColors() {
        // Cycle colors independently of system clock (prevents flashing when adjusting transition time on config screen)
        if (lastMillis == 0) {
            lastMillis = Instant.now().toEpochMilli();
        }
        ItemGuideColorData colorData = ActiveConfig.FILE_CLIENT.itemGuidePreset.colorData.get();
        long now = Instant.now().toEpochMilli();
        long timeDiff = now - lastMillis;
        int transitionTimeMS = colorData.transitionTimeMS().get();

        if (colorData.colors().get().size() > 1) {
            cycleProgress = (cycleProgress + (float) timeDiff / ((long) transitionTimeMS * colorData.colors().get().size())) % 1f;
        }
        if (colorData.selectedColors().get().size() > 1) {
            cycleProgressSelected = (cycleProgressSelected + (float) timeDiff / ((long) transitionTimeMS * colorData.selectedColors().get().size())) % 1f;
        }
        if (colorData.rangedGrabColors().get().size() > 1) {
            cycleProgressRangedGrab = (cycleProgressRangedGrab + (float) timeDiff / ((long) transitionTimeMS * colorData.rangedGrabColors().get().size())) % 1f;
        }

        itemGuideColor = updateColor(colorData.colors().get(), cycleProgress);
        itemGuideSelectedColor = updateColor(colorData.selectedColors().get(), cycleProgressSelected);
        rangedGrabColor = updateColor(colorData.rangedGrabColors().get(), cycleProgressRangedGrab);

        lastMillis = now;
    }

    public static void resetCycleProgresses() {
        cycleProgress = 0;
        cycleProgressSelected = 0;
        cycleProgressRangedGrab = 0;
    }

    protected static <I extends ImmersiveInfo> void renderInfos(Immersive<I, ?> singleton,
                                                                PoseStack stack) {
        try {
            if (singleton.isVROnly() && !VRVerify.clientInVR()) {
                return;
            }
            for (I info : singleton.getTrackedObjects()) {
                if (singleton.shouldRender(info)) {
                    singleton.render(info, stack, ImmersiveRenderHelpers.instance(), Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
                }
            }
        } catch (ConcurrentModificationException ignored) {
            // Skip rendering if the list is modified mid-render
            // It's fine, since we were only going to read it anyway!!
        }
    }

    protected static <I extends AbstractPlayerAttachmentInfo> void renderInfos(AbstractPlayerAttachmentImmersive<I, ?> singleton,
                                                                               PoseStack stack) {
        try {
            if (singleton.isVROnly() && !VRVerify.clientInVR()) {
                return;
            }
            for (I info : singleton.getTrackedObjects()) {
                singleton.doRender(info, stack, VRVerify.clientInVR());
            }
        } catch (ConcurrentModificationException ignored) {
            // Skip rendering if the list is modified mid-render
            // It's fine, since we were only going to read it anyway!!
        }
    }

    private static void renderItemGuide(PoseStack stack, BoundingBox hitbox, float alpha, boolean isSelected, int light) {
        if (hitbox != null && !Minecraft.getInstance().options.hideGui) {
            RGBA color = isSelected ? itemGuideSelectedColor() : itemGuideColor();
            AABB aabb = hitbox.isAABB() ? hitbox.asAABB() : hitbox.asOBB().getUnderlyingAABB();
            float size = (float) aabb.getSize() * (isSelected ? (float) ActiveConfig.active().itemGuideSelectedSize : (float) ActiveConfig.active().itemGuideSize);
            if (ActiveConfig.active().placementGuideMode == PlacementGuideMode.CUBE) {
                Camera renderInfo = Minecraft.getInstance().gameRenderer.getMainCamera();
                Vec3 pos = aabb.getCenter();
                stack.pushPose();
                stack.translate(-renderInfo.position().x + pos.x,
                        -renderInfo.position().y + pos.y,
                        -renderInfo.position().z + pos.z);
                MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                if (hitbox.isOBB()) {
                    OBBClientUtil.rotateStackForOBB(stack, hitbox.asOBB());
                }
                cubeModel.render(stack, buffer.getBuffer(RenderType.entityTranslucent(Cube1x1.textureLocation)),
                        (int) color.toLong(), size / 2, light);
                stack.popPose();
            } else if (ActiveConfig.active().placementGuideMode == PlacementGuideMode.OUTLINE) {
                if (hitbox.isAABB()) {
                    ImmersiveRenderHelpers.instance().renderHitbox(stack, AABB.ofSize(aabb.getCenter(), size, size, size), true,
                            color.redF(), color.greenF(), color.blueF(), color.alphaF());
                } else {
                    OBBClientUtil.renderOBB(stack, hitbox.asOBB(), true, color.redF(), color.greenF(), color.blueF(), color.alphaF());
                }
            }
        }
    }

    public static RGBA itemGuideColor() {
        return itemGuideColor;
    }

    public static RGBA itemGuideSelectedColor() {
        return itemGuideSelectedColor;
    }

    public static RGBA rangedGrabColor() {
        return rangedGrabColor;
    }

    private static RGBA updateColor(List<RGBA> colors, float cycleProgress) {
        if (colors.size() == 1) {
            return colors.get(0);
        }
        float progressPerColor = 1f / colors.size();
        int startIndex = (int) (cycleProgress / progressPerColor);
        int endIndex = startIndex + 1;
        if (endIndex == colors.size()) {
            endIndex = 0;
        }
        RGBA start = colors.get(startIndex);
        RGBA end = colors.get(endIndex);
        float transitionProgress = ((cycleProgress % progressPerColor) / progressPerColor);
        return new RGBA(
                avgColorTransition(start, end, 'r', transitionProgress),
                avgColorTransition(start, end, 'g', transitionProgress),
                avgColorTransition(start, end, 'b', transitionProgress),
                avgColorTransition(start, end, 'a', transitionProgress)
        );
    }

    private static int avgColorTransition(RGBA start, RGBA end, char c, float transitionProgress) {
        return (int) (start.getColor(c) * (1 - transitionProgress) + end.getColor(c) * transitionProgress);
    }

    public record ItemGuideRenderData(PoseStack stack, BoundingBox hitbox, float alpha, boolean isSelected, int light) {}

}
