package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.ImmersiveHitboxesInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
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
    private int backpackCooldown = 0;

    public ImmersiveHitboxes() {
        super(1);
        this.forceDisableItemGuide = true;
        this.forceTickEvenIfNoTrack = true;
    }

    @Override
    protected void doTick(ImmersiveHitboxesInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (ActiveConfig.reachBehindBackpack) {
            // centerPos is the center of the back of the player
            Vec3 centerPos = Minecraft.getInstance().player.position().add(0, 1.0625, 0);
            Vec3 headLook;
            // Even though it's VR-Only, let's try to get something so we can send the failure bag open message
            // and for desktop testing purposes
            if (VRPluginVerify.clientInVR() && VRPlugin.API.playerInVR(Minecraft.getInstance().player)
            && !Platform.isDevelopmentEnvironment()) {
                headLook = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getHMD().getLookAngle();
            } else {
                headLook = Minecraft.getInstance().player.getLookAngle();
            }
            headLook = headLook.multiply(1, 0, 1).normalize(); // Ignore y rotation
            centerPos = centerPos.add(headLook.scale(-0.25));
            // Back is 0.5 blocks across from center, making size 0.5 for x and z.
            // We swap x and z since if we're looking along z, we want it to be big on the x axis and vice-versa
            // Add 0.25 to have some sane minimum
            info.setHitbox(ImmersiveHitboxesInfo.BACKPACK_INDEX,
                    AABB.ofSize(centerPos,
                            Math.max(Math.abs(headLook.z) * 0.5, 1d/4d),
                            backpackHeight,
                            Math.max(Math.abs(headLook.x) * 0.5, 1d/4d)
                    ));
        } else {
            // In case setting changes mid-game
            info.setHitbox(ImmersiveHitboxesInfo.BACKPACK_INDEX, null);
        }
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
        AABB backpackHitbox = info.getHitbox(ImmersiveHitboxesInfo.BACKPACK_INDEX);
        if (backpackHitbox != null) {
            renderHitbox(stack, backpackHitbox, backpackHitbox.getCenter());
        }
    }

    @Override
    public boolean enabledInConfig() {
        return true; // We always have this enabled in config
    }

    @Override
    protected boolean slotShouldRenderHelpHitbox(ImmersiveHitboxesInfo info, int slotNum) {
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
    public AbstractImmersive<? extends AbstractImmersiveInfo> getSingleton() {
        return Immersives.immersiveHitboxes;
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
        if (closest == ImmersiveHitboxesInfo.BACKPACK_INDEX && hand == InteractionHand.OFF_HAND
        && backpackCooldown <= 0) {
            ClientUtil.openBag(player);
            backpackCooldown = 50;
        }
    }

    @Override
    public BlockPos getLightPos(ImmersiveHitboxesInfo info) {
        return info.getBlockPosition();
    }

    public void initImmersiveIfNeeded() {
        if (this.infos.size() == 0) {
            this.infos.add(new ImmersiveHitboxesInfo());
        }
    }
}
