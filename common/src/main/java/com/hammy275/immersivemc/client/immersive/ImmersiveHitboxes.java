package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.client.ImmersiveConfigScreenInfo;
import com.hammy275.immersivemc.api.client.ImmersiveRenderHelpers;
import com.hammy275.immersivemc.api.client.immersive.PlayerAttachmentImmersive;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.ImmersiveHitboxesInfo;
import com.hammy275.immersivemc.common.api_impl.hitbox.HitboxInfoImpl;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.data.VRBodyPartData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Used for hitboxes attached to the player
 */
public class ImmersiveHitboxes implements PlayerAttachmentImmersive<ImmersiveHitboxesInfo, ImmersiveHitboxesInfo.RenderState, NullStorage> {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final double backpackHeight = 0.625;
    private static final Vec3 DOWN = new Vec3(0, -1, 0);

    protected final List<ImmersiveHitboxesInfo> trackedObjects = new ArrayList<>();

    @Override
    public ImmersiveHitboxesInfo buildInfo(AbstractClientPlayer player) {
        return new ImmersiveHitboxesInfo();
    }

    @Override
    public Collection<ImmersiveHitboxesInfo> getTrackedObjects() {
        return trackedObjects;
    }

    @Override
    public int handleHitboxInteract(ImmersiveHitboxesInfo info, LocalPlayer player, List<Integer> hitboxIndices, InteractionHand hand, boolean modifierPressed) {
        if (hand == InteractionHand.OFF_HAND) {
            int index = hitboxIndices.get(0);
            if (index == ImmersiveHitboxesInfo.BAG_BACK_INDEX) {
                if (ActiveConfig.active().requireTriggerForBagOpen) {
                    info.canOpen = true;
                } else {
                    ClientUtil.openBag(mc.player, true);
                    return 50;
                }
            }
        }
        return -1;
    }

    @Override
    public void tick(ImmersiveHitboxesInfo info) {
        info.canOpen = false;
        info.tickCount++;
        if (ActiveConfig.active().reachBehindBagMode.usesBehindBack() && VRVerify.clientInVR()) {
            // centerPos is the center of the back of the player
            VRBodyPartData hmdData = Platform.COMMON.isDevelopmentEnvironment() ? null : VR.ClientAPI.getWorldRenderPose().getHead();
            Vec3 centerPos = hmdData != null ?
                    hmdData.getPos().add(0, -0.5, 0).add(hmdData.getDir().scale(-0.15)) :
                    mc.player.getEyePosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)).add(0, -0.5, 0).add(mc.player.getLookAngle().scale(-0.15));
            double yaw;
            Vec3 headLook;
            if (VRVerify.playerInVR(mc.player) && !Platform.COMMON.isDevelopmentEnvironment()) {
                yaw = hmdData.getYaw();
                headLook = hmdData.getDir();
            } else {
                // Yaw based on player's yaw for testing in dev
                yaw = Math.toRadians(mc.player.getYRot());
                headLook = mc.player.getLookAngle();
            }
            headLook = headLook.multiply(1, 0, 1).normalize(); // Ignore y rotation
            centerPos = centerPos.add(headLook.scale(-0.25));
            // Back is 0.5 blocks across from center, making size 0.35 longways (full back has funny accidental detections).
            // Since +Z is 0 yaw, we make the length across the back 0.35 on the X-axis.
            // Add 0.2 to have some sane minimum
            info.hitboxes.set(ImmersiveHitboxesInfo.BAG_BACK_INDEX,
                    new HitboxInfoImpl(OBBFactory.instance().create(AABB.ofSize(centerPos, 0.35, backpackHeight, 0.2),
                            0, yaw, 0), true));
        } else {
            // In case setting changes mid-game
            info.hitboxes.set(0, null);
        }

        if (!info.canOpen && ActiveConfig.active().reachBehindBagMode.usesOverShoulder() && VRVerify.clientInVR()) {
            InteractionHand hand = getBagHand();
            VRBodyPartData hmdData = VR.ClientAPI.getWorldRenderPose().getHead();
            VRBodyPartData handData = VR.ClientAPI.getWorldRenderPose().getHand(hand);

            Vec3 hmdDir = hmdData.getDir();
            Vec3 hmdPos = hmdData.getPos();
            Vec3 cDir = handData.getDir();
            Vec3 cPos = handData.getPos();

            Vec3 cToHMDDir = cPos.subtract(hmdPos).normalize(); // Angle for c to "look at" HMD.

            double angleToDown = Math.acos(DOWN.dot(cDir)); // Angle in radians between straight down and the controller dir
            boolean pointingDown = angleToDown < Math.PI / 2d;
            double cHMDAngleDiff = Math.acos(cToHMDDir.dot(hmdDir));
            boolean behindHMD = cHMDAngleDiff > 2 * Math.PI / 3d;

            if (pointingDown && behindHMD) {
                info.canOpen = true;
            }
        }
    }

    @Override
    public @Nullable BoundingBox getDragHitbox(ImmersiveHitboxesInfo info) {
        return null;
    }

    @Override
    public boolean isInputHitbox(ImmersiveHitboxesInfo info, int hitboxIndex) {
        return false;
    }

    @Override
    public boolean shouldRender(ImmersiveHitboxesInfo.RenderState renderState) {
        return true;
    }

    @Override
    public void render(ImmersiveHitboxesInfo.RenderState renderState, PoseStack stack, ImmersiveRenderHelpers helpers, float partialTick) {
        for (int i = 0; i < renderState.hitboxes.size(); i++) {
            BoundingBox hitbox = renderState.hitboxes.get(i);
            helpers.renderHitbox(stack, hitbox, false, i == renderState.slotHovered ? 0f : 1f, i == renderState.slotHovered ? 0f : 1f, 1f);
        }
    }

    @Override
    public PlayerAttachmentImmersiveHandler<NullStorage> getHandler() {
        return ImmersiveHandlers.hitboxesHandler;
    }

    @Override
    public @Nullable ImmersiveConfigScreenInfo configScreenInfo() {
        return null;
    }

    @Override
    public void processStorageFromNetwork(ImmersiveHitboxesInfo info, NullStorage storage) {
        // Client-authoritative, no networking
    }

    @Override
    public ImmersiveHitboxesInfo.RenderState createRenderState() {
        return new ImmersiveHitboxesInfo.RenderState();
    }

    @Override
    public void extractRenderState(ImmersiveHitboxesInfo info, ImmersiveHitboxesInfo.RenderState renderState, float partialTicks) {
        renderState.hitboxes = info.hitboxes.stream().map(HitboxInfoImpl::getHitbox).toList();
        renderState.ticksExisted = info.tickCount;
        renderState.slotHovered = info.slotHovered;
    }

    public void initImmersiveIfNeeded() {
        if (trackedObjects.isEmpty()) {
            trackedObjects.add(new ImmersiveHitboxesInfo());
        }
    }

    public boolean canOpenBagFromInteractModule(InteractionHand bagHand) {
        return bagHand == getBagHand() && !trackedObjects.isEmpty() &&
                trackedObjects.get(0).canOpen && ActiveConfig.active().requireTriggerForBagOpen;
    }

    private static InteractionHand getBagHand() {
        return ActiveConfig.active().swapBagHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }
}
