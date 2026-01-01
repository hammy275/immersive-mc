package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.AbstractPlayerAttachmentInfo;
import com.hammy275.immersivemc.client.immersive.info.ImmersiveHitboxesInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;

/**
 * Used for hitboxes attached to the player
 */
public class ImmersiveHitboxes extends AbstractPlayerAttachmentImmersive<ImmersiveHitboxesInfo, NullStorage> {

    private static final Minecraft mc = Minecraft.getInstance();
    
    private static final double backpackHeight = 0.625;
    private static final Vec3 DOWN = new Vec3(0, -1, 0);
    private int backpackCooldown = 0; // Used for those with trigger-hit for opening the bag disabled
    private boolean canOpenBackpack = false;

    public ImmersiveHitboxes() {
        super(1);
        this.forceDisableItemGuide = true;
        this.forceTickEvenIfNoTrack = true;
    }

    @Override
    protected void renderTick(ImmersiveHitboxesInfo info, boolean isInVR) {
        super.renderTick(info, isInVR);
        canOpenBackpack = false;
        if (ActiveConfig.active().reachBehindBagMode.usesBehindBack() && VRVerify.clientInVR()) {
            // centerPos is the center of the back of the player
            VRBodyPartData hmdData = Platform.isDevelopmentEnvironment() ? null : VRClientAPI.instance().getWorldRenderPose().getHead();
            Vec3 centerPos = hmdData != null ?
                    hmdData.getPos().add(0, -0.5, 0).add(hmdData.getDir().scale(-0.15)) :
                    mc.player.getEyePosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)).add(0, -0.5, 0).add(mc.player.getLookAngle().scale(-0.15));
            double yaw;
            Vec3 headLook;
            if (VRVerify.playerInVR(mc.player) && !Platform.isDevelopmentEnvironment()) {
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
            info.setHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX,
                    OBBFactory.instance().create(AABB.ofSize(centerPos, 0.35, backpackHeight, 0.2),
                            0, yaw, 0));
            if (BoundingBox.contains(info.getHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX), VRClientAPI.instance().getWorldRenderPose().getHand(getBagHand()).getPos())) {
                canOpenBackpack = true;
            }
        } else {
            // In case setting changes mid-game
            info.setHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX, null);
        }

        if (!canOpenBackpack && ActiveConfig.active().reachBehindBagMode.usesOverShoulder() && VRVerify.clientInVR()) {
            InteractionHand hand = getBagHand();
            VRBodyPartData hmdData = VRClientAPI.instance().getWorldRenderPose().getHead();
            VRBodyPartData handData = VRClientAPI.instance().getWorldRenderPose().getHand(hand);

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
                canOpenBackpack = true;
            }
        }

        // Handle those that don't use the trigger press to open the bag
        if (!ActiveConfig.active().requireTriggerForBagOpen && canOpenBackpack && backpackCooldown <= 0) {
            ClientUtil.openBag(mc.player, true);
            backpackCooldown = 50;
        }
    }

    @Override
    public @Nullable ImmersiveHandler getHandler() {
        return null;
    }

    @Override
    protected void doTick(ImmersiveHitboxesInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (backpackCooldown > 0) {
            backpackCooldown--;
        }
    }

    @Override
    public boolean shouldRender(ImmersiveHitboxesInfo info, boolean isInVR) {
        return true;
    }

    @Override
    protected void render(ImmersiveHitboxesInfo info, PoseStack stack, boolean isInVR) {
        BoundingBox backpackHitbox = info.getHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX);
        if (backpackHitbox != null) {
            renderHitbox(stack, backpackHitbox);
            if (VRVerify.playerInVR(mc.player) && mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)) {
                VRBodyPartData c = VRAPI.instance().getVRPose(mc.player).getHand(getBagHand());
                if (BoundingBox.contains(backpackHitbox, c.getPos())) {
                    renderHitbox(stack, AABB.ofSize(c.getPos(), 0.25, 0.25, 0.25),
                            true,
                            0f, 1f, 0f);
                }
            }
        }
    }

    @Override
    public boolean enabledInConfig() {
        return true; // We always have this enabled in config
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(ImmersiveHitboxesInfo info, int slotNum) {
        return false; // No help hitboxes
    }

    @Override
    public boolean shouldTrack(BlockPos pos, Level level) {
        return true; // Prevents info instances from being removed. Okay to do since trackObject() is a no-op.
    }

    @Override
    public ImmersiveHitboxesInfo refreshOrTrackObject(BlockPos pos, Level level) {
        // Return null. Never tracking any objects.
        return null;
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractPlayerAttachmentInfo info) {
        return false; // Doesn't really matter, never hooked into a block anyways
    }

    @Override
    protected void initInfo(ImmersiveHitboxesInfo info) {
        // No need to init, all init things are done in doTick, which needs to run every tick anyways
    }

    @Override
    public void handleRightClick(AbstractPlayerAttachmentInfo info, Player player, int closest, InteractionHand hand) {
        // Intentionally empty, all hitbox logic is handled from ticking
    }

    @Override
    public void processStorageFromNetwork(AbstractPlayerAttachmentInfo info, NullStorage storage) {
        // Intentional NO-OP
    }

    @Override
    public BlockPos getLightPos(ImmersiveHitboxesInfo info) {
        return info.getBlockPosition();
    }

    public void initImmersiveIfNeeded() {
        if (this.infos.isEmpty()) {
            this.infos.add(new ImmersiveHitboxesInfo());
        }
    }

    public boolean canOpenBagFromInteractModule(InteractionHand bagHand) {
        return bagHand == getBagHand() && canOpenBackpack && ActiveConfig.active().requireTriggerForBagOpen;
    }

    private static InteractionHand getBagHand() {
        return ActiveConfig.active().swapBagHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

}
