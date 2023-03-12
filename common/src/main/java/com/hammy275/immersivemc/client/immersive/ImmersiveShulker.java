package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.ShulkerInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ImmersiveShulker extends AbstractBlockEntityImmersive<ShulkerBoxBlockEntity, ShulkerInfo> {
    public ImmersiveShulker() {
        super(4);
    }

    @Override
    public ShulkerInfo getNewInfo(BlockEntity tileEnt) {
        return new ShulkerInfo((ShulkerBoxBlockEntity) tileEnt, ClientConstants.ticksToRenderShulker);
    }

    @Override
    public int getTickTime() {
        return ClientConstants.ticksToRenderShulker;
    }

    @Override
    public boolean shouldRender(ShulkerInfo info, boolean isInVR) {
        return info.readyToRender() && info.isOpen;
    }

    @Override
    protected void render(ShulkerInfo info, PoseStack stack, boolean isInVR) {
        float itemSize = ClientConstants.itemScaleSizeShulker / info.getItemTransitionCountdown();
        for (int i = 0; i < 27; i++) {
            float renderSize = info.slotHovered == i ? itemSize * 1.25f : itemSize;
            renderItem(info.items[i], stack, info.getPosition(i), renderSize,
                    info.viewForwardDir, info.upDownRender,
                    info.getHitbox(i), true, -1, info.light);
        }
    }

    @Override
    protected boolean enabledInConfig() {
        return ActiveConfig.useShulkerImmersion;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isShulkerBox(pos, state, tileEntity, level);
    }

    @Override
    public AbstractImmersive<? extends AbstractImmersiveInfo> getSingleton() {
        return Immersives.immersiveShulker;
    }

    @Override
    protected void initInfo(ShulkerInfo info) {
        setHitboxes(info);
    }

    public void setHitboxes(ShulkerInfo info) {
        BlockEntity shulker = Minecraft.getInstance().level.getBlockEntity(info.getBlockPosition());
        Direction forward = shulker.getBlockState().getValue(ShulkerBoxBlock.FACING);
        Vec3 forwardVec = new Vec3(forward.getNormal().getX(), forward.getNormal().getY(), forward.getNormal().getZ());
        Vec3[] positions;
        info.upDownRender = null;
        if (forward == Direction.DOWN || forward == Direction.UP) {
            positions = get3x3VerticalGrid(info.getBlockPosition(), 0.15);
            for (int i = 0; i < positions.length; i++) {
                positions[i] = positions[i].add(forwardVec.scale(0.25));
            }
            info.viewForwardDir = getForwardFromPlayer(Minecraft.getInstance().player);
        } else {
            Direction left = forward.getCounterClockWise();
            Vec3 leftVec = new Vec3(left.getNormal().getX(), left.getNormal().getY(),
                    left.getNormal().getZ());
            Vec3 centerPos = Vec3.atCenterOf(info.getBlockPosition());

            Vec3 leftPos = centerPos.add(leftVec.scale(0.5));
            Vec3 rightPos = centerPos.add(leftVec.scale(-0.5));
            Vec3 topPos = centerPos.add(0, 0.5, 0);
            Vec3 botPos = centerPos.add(0, -0.5, 0);

            Vec3 playerPos = Minecraft.getInstance().player.getEyePosition();

            double leftDist = playerPos.distanceToSqr(leftPos);
            double rightDist = playerPos.distanceToSqr(rightPos);
            double topDist = playerPos.distanceToSqr(topPos);
            double botDist = playerPos.distanceToSqr(botPos);

            double min = Math.min(leftDist, rightDist);
            min = Math.min(min, topDist);
            min = Math.min(min, botDist);

            if (min == leftDist) { // Closest to left
                positions = get3x3VerticalGrid(info.getBlockPosition(), 0.15, forward.getCounterClockWise());
                for (int i = 0; i < positions.length; i++) {
                    positions[i] = positions[i].add(forwardVec.scale(0.25));
                }
                info.viewForwardDir = left;
            } else if (min == rightDist) {
                positions = get3x3VerticalGrid(info.getBlockPosition(), 0.15, forward.getClockWise());
                for (int i = 0; i < positions.length; i++) {
                    positions[i] = positions[i].add(forwardVec.scale(0.25));
                }
                info.viewForwardDir = left.getOpposite();
            } else if (min == topDist) {
                positions = get3x3HorizontalGrid(info.getBlockPosition(), 0.15, forward, false);
                for (int i = 0; i < positions.length; i++) {
                    positions[i] = positions[i].add(forwardVec.scale(0.25));
                }
                info.viewForwardDir = getForwardFromPlayer(Minecraft.getInstance().player);
                info.upDownRender = Direction.UP;
            } else {
                positions = get3x3HorizontalGrid(info.getBlockPosition(), 0.15, forward, false);
                for (int i = 0; i < positions.length; i++) {
                    positions[i] = positions[i].add(0, -1, 0).add(forwardVec.scale(0.25));
                }
                info.viewForwardDir = getForwardFromPlayer(Minecraft.getInstance().player);
                info.upDownRender = Direction.DOWN;
            }
        }
        for (int i = 0; i < 27; i++) {
            info.setHitbox(i, null);
            info.setPosition(i, null);
        }
        for (int i = 0; i < 9; i++) {
            info.setHitbox(i + info.getRowNum() * 9, createHitbox(positions[i], 0.075f));
            info.setPosition(i + info.getRowNum() * 9, positions[i]);
        }
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        if (((ShulkerInfo) info).isOpen) {
            Network.INSTANCE.sendToServer(new SwapPacket(info.getBlockPosition(), closest, hand));
        }

    }

    @Override
    protected void doTick(ShulkerInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        setHitboxes(info);
    }

    @Override
    public BlockPos getLightPos(ShulkerInfo info) {
        return info.getBlockPosition().relative(info.viewForwardDir);
    }

    public static void openShulkerBox(ShulkerInfo info) {
        info.isOpen = !info.isOpen;
        Network.INSTANCE.sendToServer(new ChestShulkerOpenPacket(info.getBlockPosition(), info.isOpen));
        if (!info.isOpen) {
            info.remove(); // Remove immersive if we're closing the chest
        }
    }

    @Override
    public boolean hitboxesAvailable(AbstractImmersiveInfo info) {
        return ((ShulkerInfo) info).isOpen;
    }
}
