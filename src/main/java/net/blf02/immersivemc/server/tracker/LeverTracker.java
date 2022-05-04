package net.blf02.immersivemc.server.tracker;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.tracker.AbstractTracker;
import net.blf02.immersivemc.common.vr.VRPlugin;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.immersivemc.server.PlayerConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class LeverTracker extends AbstractTracker {

    public static Map<String, LeverInfo> infos = new HashMap<>();

    public LeverTracker() {
        ServerTrackerInit.playerTrackers.add(this);
    }

    @Override
    protected void tick(PlayerEntity player) {
        LeverInfo info = infos.get(player.getGameProfile().getName());
        if (info == null) {
            info = new LeverInfo();
            infos.put(player.getGameProfile().getName(), info);
        }
        info.tick();
        if (info.cooldown > 0) return;
        for (int c = 0; c <= 1; c++) {
            Vector3d handPos = VRPlugin.API.getVRPlayer(player).getController(c).position();
            BlockPos handBlockPos = new BlockPos(handPos);
            BlockPos leverPos;
            BlockState state;
            if (player.level.getBlockState(handBlockPos).getBlock() == Blocks.LEVER) {
                state = player.level.getBlockState(handBlockPos);
                leverPos = handBlockPos;
            } else if (player.level.getBlockState(handBlockPos.below()).getBlock() == Blocks.LEVER) {
                state = player.level.getBlockState(handBlockPos.below());
                leverPos = handBlockPos.below();
            } else if (player.level.getBlockState(handBlockPos.above()).getBlock() == Blocks.LEVER) {
                state = player.level.getBlockState(handBlockPos.above());
                leverPos = handBlockPos.above();
            } else {
                continue;
            }
            Vector3d hitboxPos;
            if (state.getValue(HorizontalFaceBlock.FACE) == AttachFace.WALL) {
                hitboxPos = Vector3d.atCenterOf(leverPos);
                Direction facing = state.getValue(HorizontalFaceBlock.FACING);
                Direction towardsWall = facing.getOpposite();
                Vector3d unit = Vector3d.atLowerCornerOf(towardsWall.getNormal()); // Converts Vector3i to Vector3d
                hitboxPos = hitboxPos.add(unit.multiply(0.25, 0.25, 0.25));
                if (state.getValue(LeverBlock.POWERED)) {
                    hitboxPos = hitboxPos.add(0, -1d/3d, 0);
                } else {
                    hitboxPos = hitboxPos.add(0, 1d/3d, 0);
                }
            } else {
                hitboxPos = Vector3d.atCenterOf(leverPos);
                Direction facingOn = state.getValue(HorizontalFaceBlock.FACING);
                Direction facingOff = facingOn.getOpposite();
                Vector3d unit;
                if (state.getValue(LeverBlock.POWERED)) {
                    unit = Vector3d.atLowerCornerOf(facingOn.getNormal());
                } else {
                    unit = Vector3d.atLowerCornerOf(facingOff.getNormal());
                }
                hitboxPos = hitboxPos.add(unit.multiply(0.25, 0.25, 0.25));
            }
            double size = 0.25;
            AxisAlignedBB hitbox = new AxisAlignedBB(hitboxPos.x - size, hitboxPos.y - size, hitboxPos.z - size,
                    hitboxPos.x + size, hitboxPos.y + size, hitboxPos.z + size);
            if (hitbox.contains(handPos)) {
                Vector3d last = info.getLastPos(c);
                if (last.distanceToSqr(handPos) > 0.1*0.1) { // If our hand moved a significant amount
                    info.cooldown = 20;
                    LeverBlock leverBlock = (LeverBlock) Blocks.LEVER;
                    leverBlock.use(state, player.level, leverPos, player, c == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND,
                            null);
                }
            }
            info.setLastPos(c, handPos);
        }
    }

    @Override
    protected boolean shouldTick(PlayerEntity player) {
        return ActiveConfig.useLever &&
                VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player)
                && PlayerConfigs.getConfig(player).useLevers;
    }

    public static class LeverInfo {
        public int cooldown = 0;
        public Vector3d lastPosMain = Vector3d.ZERO;
        public Vector3d lastPosOff = Vector3d.ZERO;

        public void tick() {
            if (cooldown > 0) {
                cooldown--;
            }
        }

        public Vector3d getLastPos(int c) {
            return c == 0 ? lastPosMain : lastPosOff;
        }

        public void setLastPos(int c, Vector3d pos) {
            if (c == 0) {
                lastPosMain = pos;
            } else {
                lastPosOff = pos;
            }
        }
    }
}
