package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.LeverInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.UsePacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ImmersiveLever extends AbstractImmersive<LeverInfo> {
    public ImmersiveLever() {
        super(-1);
    }

    @Override
    public boolean isVROnly() {
        return true;
    }

    @Override
    public boolean clientAuthoritative() {
        return true;
    }

    @Override
    public @Nullable ImmersiveHandler getHandler() {
        return null;
    }

    @Override
    public boolean shouldRender(LeverInfo info, boolean isInVR) {
        return info.readyToRender();
    }

    @Override
    protected void render(LeverInfo info, PoseStack stack, boolean isInVR) {
        for (int i = 0; i <= 1; i++) {
            renderHitbox(stack, info.getHitbox(i));
        }
    }

    @Override
    protected void doTick(LeverInfo info, boolean isInVR) {
        super.doTick(info, isInVR);

        BlockState lever = Minecraft.getInstance().level.getBlockState(info.getBlockPosition());
        boolean powered = lever.getValue(BlockStateProperties.POWERED);
        int startHitbox = powered ? 1 : 0;
        int endHitbox = powered ? 0 : 1;

        for (int c = 0; c <= 1; c++) {
            IVRData hand = VRPlugin.API.getVRPlayer(Minecraft.getInstance().player).getController(c);
            int lastGrabbed = info.grabbedBox[c];
            int grabbed = Util.getFirstIntersect(hand.position(), info.getAllHitboxes()).orElse(-1);
            info.grabbedBox[c] = grabbed;
            if (grabbed == endHitbox && lastGrabbed == startHitbox) {
                Util.useLever(Minecraft.getInstance().player, info.getBlockPosition());
                Network.INSTANCE.sendToServer(new UsePacket(info.getBlockPosition()));
            }
        }

    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.active().useLever;
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(LeverInfo info, int slotNum) {
        return false;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, Level level) {
        return ImmersiveCheckers.isLever(pos, level);
    }

    @Override
    public @Nullable LeverInfo refreshOrTrackObject(BlockPos pos, Level level) {
        for (LeverInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderLever);
                return info;
            }
        }
        LeverInfo info = new LeverInfo(pos);
        infos.add(info);
        return info;
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    protected void initInfo(LeverInfo info) {
        Level level = Minecraft.getInstance().level;
        BlockState state = level.getBlockState(info.getBlockPosition());
        Vec3 center = Vec3.atCenterOf(info.getBlockPosition());
        AttachFace attachFace = state.getValue(BlockStateProperties.ATTACH_FACE);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction towardsBaseDir;
        Direction towardsOnDir;
        switch (attachFace) {
            case WALL -> {
                towardsBaseDir = facing.getOpposite();
                towardsOnDir = Direction.DOWN;
            }
            case CEILING -> {
                towardsBaseDir = Direction.UP;
                towardsOnDir = facing;
            }
            case FLOOR -> {
                towardsBaseDir = Direction.DOWN;
                towardsOnDir = facing;
            }
            default -> throw new IllegalStateException("Lever is attached to unknown face " + attachFace.getSerializedName());
        }
        Vec3 towardsBase = Vec3.atLowerCornerOf(towardsBaseDir.getNormal());
        Vec3 towardsOn = Vec3.atLowerCornerOf(towardsOnDir.getNormal());
        center = center.add(towardsBase.scale(0.25));

        Vec3 offPos = center.add(towardsOn.scale(-0.25));
        Vec3 onPos = center.add(towardsOn.scale(0.25));

        info.setPosition(0, offPos);
        info.setPosition(1, onPos);

        info.setHitbox(0, AABB.ofSize(offPos, 0.5, 0.5, 0.5));
        info.setHitbox(1, AABB.ofSize(onPos, 0.5, 0.5, 0.5));
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        // NO-OP. Handled in doTick()
    }

    @Override
    public void processStorageFromNetwork(AbstractImmersiveInfo info, NetworkStorage storage) {
        // NO-OP. No storage.
    }

    @Override
    public BlockPos getLightPos(LeverInfo info) {
        return info.getBlockPosition();
    }
}
