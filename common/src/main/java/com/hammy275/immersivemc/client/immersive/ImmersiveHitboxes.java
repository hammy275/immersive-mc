package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.ImmersiveHitboxesInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Used for hitboxes attached to the player
 */
public class ImmersiveHitboxes extends AbstractImmersive<ImmersiveHitboxesInfo> {

    private static final double backpackHeight = 0.625;
    private static final Vec3 DOWN = new Vec3(0, -1, 0);
    private int backpackCooldown = 0;

    public ImmersiveHitboxes() {
        super(1);
        this.forceDisableItemGuide = true;
        this.forceTickEvenIfNoTrack = true;
    }

    @Override
    protected void renderTick(ImmersiveHitboxesInfo info, boolean isInVR) {
        super.renderTick(info, isInVR);
        if (ActiveConfig.reachBehindBackpackMode.usesBehindBack() && VRPluginVerify.clientInVR()) {
            // centerPos is the center of the back of the player
            IVRData hmdData = Platform.isDevelopmentEnvironment() ?
                    VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getHMD() :
                    VRPlugin.API.getRenderVRPlayer().getHMD();
            Vec3 centerPos = hmdData.position().add(0, -0.5, 0).add(hmdData.getLookAngle().scale(-0.15));
            Vec3 headLook;
            // Even though it's VR-Only, let's try to get something for desktop testing purposes
            if (VRPluginVerify.clientInVR() && VRPlugin.API.playerInVR(Minecraft.getInstance().player)
                    && !Platform.isDevelopmentEnvironment()) {
                headLook = hmdData.getLookAngle();
            } else {
                headLook = Minecraft.getInstance().player.getLookAngle();
            }
            headLook = headLook.multiply(1, 0, 1).normalize(); // Ignore y rotation
            centerPos = centerPos.add(headLook.scale(-0.25));
            // Back is 0.5 blocks across from center, making size 0.35 for x and z (full back has funny accidental detections).
            // We swap x and z since if we're looking along z, we want it to be big on the x axis and vice-versa
            // Add 0.2 to have some sane minimum
            info.setHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX,
                    AABB.ofSize(centerPos,
                            Math.max(Math.abs(headLook.z) * 0.35, 0.2),
                            backpackHeight,
                            Math.max(Math.abs(headLook.x) * 0.35, 0.2)
                    ));
        } else {
            // In case setting changes mid-game
            info.setHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX, null);
        }

        if (ActiveConfig.reachBehindBackpackMode.usesOverShoulder() && VRPluginVerify.clientInVR()) {
            IVRData hmdData = Platform.isDevelopmentEnvironment() ?
                    VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getHMD() :
                    VRPlugin.API.getRenderVRPlayer().getHMD();
            IVRData c1Data = Platform.isDevelopmentEnvironment() ?
                    VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController1() :
                    VRPlugin.API.getRenderVRPlayer().getController1();

            Vec3 hmdDir = hmdData.getLookAngle();
            Vec3 hmdPos = hmdData.position();
            Vec3 c1Dir = c1Data.getLookAngle();
            Vec3 c1Pos = c1Data.position();

            Vec3 c1ToHMDDir = c1Pos.subtract(hmdPos).normalize(); // Angle for c1 to "look at" HMD.

            double angleToDown = Math.acos(DOWN.dot(c1Dir)); // Angle in radians between straight down and the controller dir
            boolean pointingDown = angleToDown < Math.PI / 2d;
            double c1HMDAngleDiff = Math.acos(c1ToHMDDir.dot(hmdDir));
            boolean behindHMD = c1HMDAngleDiff > 2 * Math.PI / 3d;

            if (pointingDown && behindHMD) {
                doBagOpen(Minecraft.getInstance().player);
            }
        }
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
        AABB backpackHitbox = info.getHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX);
        if (backpackHitbox != null) {
            renderHitbox(stack, backpackHitbox, backpackHitbox.getCenter());
            if (VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(Minecraft.getInstance().player)
            && Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                IVRData c1 = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController1();
                if (backpackHitbox.contains(c1.position())) {
                    renderHitbox(stack, AABB.ofSize(c1.position(), 0.25, 0.25, 0.25),
                            c1.position(), true,
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
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return false; // No blocks need to be tracked
    }

    @Override
    public void trackObject(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        // NO-OP. Never tracking any objects.
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return false; // Doesn't really matter, never hooked into a block anyways
    }

    @Override
    protected void initInfo(ImmersiveHitboxesInfo info) {
        // No need to init, all init things are done in doTick, which needs to run every tick anyways
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        if (info instanceof ImmersiveHitboxesInfo hInfo) {
            if (closest == ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX && hand == InteractionHand.OFF_HAND) {
                doBagOpen(player);
            }
        }

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

    private void doBagOpen(Player player) {
        if (backpackCooldown <= 0) {
            VRRumble.rumbleIfVR(null, 1, CommonConstants.vibrationTimePlayerActionAlert);
            ClientUtil.openBag(player);
            backpackCooldown = 50;
        }
    }
}
