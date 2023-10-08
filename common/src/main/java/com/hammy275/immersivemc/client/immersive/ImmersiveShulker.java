package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.ShulkerInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import com.mojang.blaze3d.vertex.PoseStack;
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
        int minRange = info.getRowNum() * 9;
        int maxRange = (info.getRowNum() * 9) + 8;
        for (int i = 0; i < 27; i++) {
            float renderSize = info.slotHovered == i ? itemSize * 1.25f : itemSize;
            boolean showCount = i >= minRange && i <= maxRange;
            renderItem(info.items[i], stack, info.getPosition(i), renderSize,
                    info.viewForwardDir, info.upDownRender,
                    info.getHitbox(i), showCount, -1, info.light);
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useShulkerImmersion;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isShulkerBox(pos, state, tileEntity, level);
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return true;
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
        Direction towardsBoxInside;
        if (forward == Direction.DOWN || forward == Direction.UP) {
            positions = get3x3VerticalGrid(info.getBlockPosition(), 0.15);
            for (int i = 0; i < positions.length; i++) {
                positions[i] = positions[i].add(forwardVec.scale(0.25));
            }
            info.viewForwardDir = getForwardFromPlayer(Minecraft.getInstance().player);
            towardsBoxInside = info.viewForwardDir.getOpposite();
            // Set these so we don't create memory every tick for a new array
            info.lightPositions[0] = shulker.getBlockPos().relative(Direction.NORTH);
            info.lightPositions[1] = shulker.getBlockPos().relative(Direction.EAST);
            info.lightPositions[2] = shulker.getBlockPos().relative(Direction.SOUTH);
            info.lightPositions[3] = shulker.getBlockPos().relative(Direction.WEST);
        } else {
            info.lightPositions[0] = shulker.getBlockPos().relative(Direction.UP);
            info.lightPositions[1] = shulker.getBlockPos().relative(Direction.DOWN);
            info.lightPositions[2] = shulker.getBlockPos().relative(forward.getClockWise());
            info.lightPositions[3] = shulker.getBlockPos().relative(forward.getCounterClockWise());

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
                towardsBoxInside = forward.getClockWise();
            } else if (min == rightDist) {
                positions = get3x3VerticalGrid(info.getBlockPosition(), 0.15, forward.getClockWise());
                for (int i = 0; i < positions.length; i++) {
                    positions[i] = positions[i].add(forwardVec.scale(0.25));
                }
                info.viewForwardDir = left.getOpposite();
                towardsBoxInside = forward.getCounterClockWise();
            } else if (min == topDist) { // On top of Shulker
                positions = get3x3HorizontalGrid(info.getBlockPosition(), 0.15, forward, false);
                for (int i = 0; i < positions.length; i++) {
                    positions[i] = positions[i].add(forwardVec.scale(0.25));
                }
                info.viewForwardDir = getForwardFromPlayer(Minecraft.getInstance().player);
                info.upDownRender = Direction.UP;
                towardsBoxInside = Direction.DOWN;
            } else { // On bottom of shulker
                positions = get3x3HorizontalGrid(info.getBlockPosition(), 0.15, forward, false);
                for (int i = 0; i < positions.length; i++) {
                    positions[i] = positions[i].add(0, -1, 0).add(forwardVec.scale(0.25));
                }
                info.viewForwardDir = getForwardFromPlayer(Minecraft.getInstance().player);
                info.upDownRender = Direction.DOWN;
                towardsBoxInside = Direction.UP;
            }
        }
        for (int i = 0; i < 27; i++) {
            info.setHitbox(i, null);
            info.setPosition(i, null);
        }
        for (int i = 0; i < 9; i++) {
            Vec3 toBackDiff = Vec3.atLowerCornerOf(towardsBoxInside.getNormal()).scale(1d/3d);
            info.setHitbox(i + info.getRowNum() * 9, createHitbox(positions[i], 0.07f));
            info.setPosition(i + info.getRowNum() * 9, positions[i]);

            info.setHitbox(i + info.getNextRow(info.getRowNum()) * 9,
                    createHitbox(positions[i].add(toBackDiff), 0.07f));
            info.setPosition(i + info.getNextRow(info.getRowNum()) * 9,
                    positions[i].add(toBackDiff));

            info.setHitbox(i + info.getNextRow(info.getNextRow(info.getRowNum())) * 9,
                    createHitbox(positions[i].add(toBackDiff.scale(2)), 0.07f));
            info.setPosition(i + info.getNextRow(info.getNextRow(info.getRowNum())) * 9,
                    positions[i].add(toBackDiff.scale(2)));
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
        throw new UnsupportedOperationException("ShulkerInfo uses multiple light positions");
    }

    @Override
    public boolean hasMultipleLightPositions(ShulkerInfo info) {
        return true;
    }

    @Override
    public BlockPos[] getLightPositions(ShulkerInfo info) {
        return info.lightPositions;
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(ShulkerInfo info, int slotNum) {
        int minRange = info.getRowNum() * 9;
        int maxRange = (info.getRowNum() * 9) + 8;
        return super.inputSlotShouldRenderHelpHitbox(info, slotNum)
                && slotNum >= minRange && slotNum <= maxRange;
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
