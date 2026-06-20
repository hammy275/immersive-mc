package com.hammy275.immersivemc.client.subscribe;

import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveRenderState;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.SwapTracker;
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
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.time.Instant;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ClientRenderSubscriber {

    public static SubmitNodeStorage collector;

    public static final Cube1x1 cubeModel = new Cube1x1(Minecraft.getInstance().getEntityModels().bakeLayer(Cube1x1.LAYER_LOCATION));

    public static final List<ItemGuideRenderData> itemGuideRenderData = new ArrayList<>(128);

    private static long lastMillis;
    private static RGBA itemGuideColor;
    private static float cycleProgress;
    private static RGBA itemGuideSelectedColor;
    private static float cycleProgressSelected;
    private static RGBA rangedGrabColor;
    private static float cycleProgressRangedGrab;

    public static void onWorldRender(PoseStack stack, SubmitNodeStorage collector) {
        ClientRenderSubscriber.collector = collector;
        setRenderColors();
        try {
            for (Immersive<?, ?, ?> singleton : Immersives.ALL_IMMERSIVES) {
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
    }

    public static void onTransparentRender(PoseStack stack) {
        // Render item guides separately so items behind item guides can be seen and transparency works
        for (ItemGuideRenderData data : itemGuideRenderData) {
            renderItemGuide(data.stack, data.hitbox, data.alpha, data.isSelected, data.light);
        }
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

    protected static <I extends ImmersiveInfo, R extends ImmersiveRenderState> void renderInfos(Immersive<I, R, ?> singleton, PoseStack stack) {
        for (I info : singleton.getTrackedObjects()) {
            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            R renderState = singleton.createRenderState();
            singleton.extractRenderState(info, renderState, partialTicks);
            SwapTracker.updateRenderStatesFromInfo(info, renderState);
            if (singleton.shouldRender(renderState)) {
                singleton.render(renderState, stack, ImmersiveRenderHelpers.instance(), partialTicks);
            }
        }
    }

    private static void renderItemGuide(PoseStack stack, BoundingBox hitbox, float alpha, boolean isSelected, int light) {
        if (hitbox != null && !Minecraft.getInstance().gameRenderer.gameRenderState().guiRenderState.isHudHidden) {
            RGBA color = isSelected ? itemGuideSelectedColor() : itemGuideColor();
            AABB aabb = hitbox.isAABB() ? hitbox.asAABB() : hitbox.asOBB().getUnderlyingAABB();
            float size = (float) aabb.getSize() * (isSelected ? (float) ActiveConfig.active().itemGuideSelectedSize : (float) ActiveConfig.active().itemGuideSize);
            if (ActiveConfig.active().placementGuideMode == PlacementGuideMode.CUBE) {
                Camera renderInfo = Minecraft.getInstance().gameRenderer.mainCamera();
                Vec3 pos = aabb.getCenter();
                stack.pushPose();
                stack.translate(-renderInfo.position().x + pos.x,
                        -renderInfo.position().y + pos.y,
                        -renderInfo.position().z + pos.z);
                if (hitbox.isOBB()) {
                    OBBClientUtil.rotateStackForOBB(stack, hitbox.asOBB());
                }
                stack.scale(size * 8f, size * 8f, size * 8f);
                ClientRenderSubscriber.collector.submitModel(cubeModel, null, stack,
                        RenderTypes.entityTranslucent(Cube1x1.textureLocation), light, OverlayTexture.NO_OVERLAY,
                        (int) color.toLong(), null, 0x00000000, null);
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
