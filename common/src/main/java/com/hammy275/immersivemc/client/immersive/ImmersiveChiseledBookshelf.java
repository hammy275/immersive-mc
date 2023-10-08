package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.config.ClientConstants;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.ChiseledBookshelfInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.InteractPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ImmersiveChiseledBookshelf extends AbstractImmersive<ChiseledBookshelfInfo> {

    private static final double LEFT_MID_CUTOFF = 0.375;
    private static final double MID_RIGHT_CUTOFF = 0.6875;

    public ImmersiveChiseledBookshelf() {
        super(1);
    }

    @Override
    public boolean isVROnly() {
        return true;
    }

    @Override
    public boolean shouldRender(ChiseledBookshelfInfo info, boolean isInVR) {
        return info.readyToRender();
    }

    @Override
    protected void render(ChiseledBookshelfInfo info, PoseStack stack, boolean isInVR) {
        for (int i = 0; i < info.getAllHitboxes().length; i++) {
            this.renderHitbox(stack, info.getHitbox(i), info.getPosition(i));
        }
    }

    @Override
    public boolean enabledInConfig() {
        return ActiveConfig.useChiseledBookshelfImmersion;
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(ChiseledBookshelfInfo info, int slotNum) {
        return false;
    }

    @Override
    public boolean shouldTrack(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        return ImmersiveCheckers.isChiseledBookshelf(pos, state, tileEntity, level);
    }

    @Override
    public void trackObject(BlockPos pos, BlockState state, BlockEntity tileEntity, Level level) {
        for (ChiseledBookshelfInfo info : getTrackedObjects()) {
            if (info.getBlockPosition().equals(pos)) {
                info.setTicksLeft(ClientConstants.ticksToRenderRepeater);
                return;
            }
        }
        infos.add(new ChiseledBookshelfInfo(pos));
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractImmersiveInfo info) {
        return false; // No GUI; no need to ever block.
    }

    @Override
    protected void initInfo(ChiseledBookshelfInfo info) {
        Direction blockForward = Minecraft.getInstance().level.getBlockState(info.getBlockPosition()).getValue(HorizontalDirectionalBlock.FACING);
        Vec3 rightVec = Vec3.atLowerCornerOf(blockForward.getCounterClockWise().getNormal());
        Vec3 botLeft = this.getDirectlyInFront(blockForward, info.getBlockPosition());

        // xMod and zMod are used for extending the hitbox into and out of the shelf
        double xMod = blockForward.getAxis() == Direction.Axis.X ? 0.125 : 0;
        double zMod = blockForward.getAxis() == Direction.Axis.Z ? 0.125 : 0;
        AABB leftHitbox = new AABB(botLeft.x() - xMod, botLeft.y(), botLeft.z() - zMod,
                botLeft.x() + rightVec.scale(LEFT_MID_CUTOFF).x() + xMod,
                botLeft.y() + 0.5,
                botLeft.z() + rightVec.scale(LEFT_MID_CUTOFF).z() + zMod);
        info.setHitbox(3, leftHitbox);
        AABB rightHitbox = new AABB(botLeft.x() - xMod + rightVec.x(), botLeft.y(), botLeft.z() - zMod + rightVec.z(),
                botLeft.x() + rightVec.scale(MID_RIGHT_CUTOFF).x() + xMod,
                botLeft.y() + 0.5,
                botLeft.z() + rightVec.scale(MID_RIGHT_CUTOFF).z() + zMod);
        info.setHitbox(5, rightHitbox);
        info.setHitbox(4, new AABB(leftHitbox.maxX, leftHitbox.maxY, leftHitbox.maxZ,
                rightHitbox.minX, rightHitbox.minY, rightHitbox.minZ));


        for (int i = 3; i < info.getAllHitboxes().length; i++) {
            info.setPosition(i, info.getHitbox(i).getCenter());
            info.setHitbox(i - 3, info.getHitbox(i).move(0, 0.5, 0));
            info.setPosition(i - 3, info.getPosition(i).add(0, 0.5, 0));
        }
    }

    @Override
    public void handleRightClick(AbstractImmersiveInfo info, Player player, int closest, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new InteractPacket(info.getBlockPosition(), closest, hand));
    }

    @Override
    public BlockPos getLightPos(ChiseledBookshelfInfo info) {
        return info.getBlockPosition(); // Unused, since there's nothing to be lit up for this immersive
    }
}
