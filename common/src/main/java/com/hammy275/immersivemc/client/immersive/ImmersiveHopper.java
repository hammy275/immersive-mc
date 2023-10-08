package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.HopperInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ImmersiveHopper extends AbstractBlockEntityImmersive<HopperBlockEntity, HopperInfo> {

    public ImmersiveHopper() {
        super(2);
    }

    protected void setHitboxes(HopperInfo info) {
        Direction forward = getForwardFromPlayer(Minecraft.getInstance().player);
        Direction forwardUnmodified = forward;

        Vec3[] positions;

        if (isAboveHopper(info.getBlockPosition(), playerPos())) {
            forward = Direction.UP;
            Vec3[] positionsRaw = get3x3HorizontalGrid(info.getBlockPosition(), ImmersiveChest.spacing,
                    forwardUnmodified, false);
            positions = new Vec3[]{positionsRaw[1], positionsRaw[3], positionsRaw[4], positionsRaw[5], positionsRaw[7]};
        } else {
            Vec3 leftUnscaled = Vec3.atLowerCornerOf(getLeftOfDirection(forward).getNormal());

            Vec3 forwardBotLeft = getDirectlyInFront(forward, info.getBlockPosition());
            Vec3 center = forwardBotLeft.add(0, 0.95 - (ClientConstants.itemScaleSizeHopper / 2d), 0).add(leftUnscaled.scale(0.5));
            Vec3 rightScaled = leftUnscaled.scale(-1).scale(ClientConstants.itemScaleSizeHopper * 1.1d);
            Vec3 leftScaled = rightScaled.scale(-1);
            positions = new Vec3[]{center.add(rightScaled).add(rightScaled), center.add(rightScaled),
                                   center,
                                   center.add(leftScaled), center.add(leftScaled).add(leftScaled)};
        }
        for (int i = 0; i < positions.length; i++) {
            info.setPosition(i, positions[i]);
            info.setHitbox(i, AABB.ofSize(positions[i],
                    ClientConstants.itemScaleSizeHopper, ClientConstants.itemScaleSizeHopper, ClientConstants.itemScaleSizeHopper));
        }

        info.lastDirectionForBoxPos = forward;
        info.lastDirectionForBoxRot = forwardUnmodified;
    }

    @Override
    protected void doTick(HopperInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        Direction forwardForBoxPos = getForwardFromPlayer(Minecraft.getInstance().player);
        Direction forwardForBoxRot = forwardForBoxPos;
        if (isAboveHopper(info.getBlockPosition(), playerPos())) {
            forwardForBoxPos = Direction.UP;
        }
        if (forwardForBoxPos != info.lastDirectionForBoxPos || forwardForBoxRot != info.lastDirectionForBoxRot) {
            setHitboxes(info);
        }
    }

    @Override
    public BlockPos getLightPos(HopperInfo info) {
        return info.getBlockPosition().relative(info.lastDirectionForBoxPos);
    }

    @Override
    public HopperInfo getNewInfo(BlockEntity tileEnt) {
        return new HopperInfo((HopperBlockEntity) tileEnt);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderHopper;
    }

    @Override
    public boolean shouldRender(HopperInfo info, boolean isInVR) {
        return info.readyToRender();
    }

    @Override
    protected void render(HopperInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeHopper / info.getItemTransitionCountdown();
        for (int i = 0; i <= 4; i++) {
            float renderSize = info.slotHovered == i ? itemSize * 1.25f : itemSize;
            Direction neswDir = info.lastDirectionForBoxPos == Direction.UP || info.lastDirectionForBoxPos == Direction.DOWN ?
                    getForwardFromPlayer(Minecraft.getInstance().player) : info.lastDirectionForBoxPos;
            renderItem(info.items[i], stack, info.getPosition(i),
                    renderSize, neswDir, info.lastDirectionForBoxPos, info.getHitbox(i), true, -1, info.light);
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useHopperImmersion;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isHopper(pos, state, tileEntity, level);
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
    }

    @Override
    protected void initInfo(HopperInfo info) {
        setHitboxes(info);
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), closest, hand));
    }

    public boolean isAboveHopper(BlockPos hopperPos, Vec3 playerPos) {
        return playerPos.y >= hopperPos.above().getY() ||
                (playerPos.distanceToSqr(Vec3.atCenterOf(hopperPos)) < (0.5 * 0.5) &&
                        playerPos.y >= hopperPos.getY() + 0.625);
    }
}
