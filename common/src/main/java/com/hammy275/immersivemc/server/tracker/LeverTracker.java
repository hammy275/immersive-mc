package com.hammy275.immersivemc.server.tracker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginProxy;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.server.PlayerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class LeverTracker extends AbstractTracker {

    public static Map<String, LeverInfo> infos = new HashMap<>();

    public LeverTracker() {
        ServerTrackerInit.playerTrackers.add(this);
    }

    @Override
    protected void tick(Player player) {
        LeverInfo info = infos.get(player.getGameProfile().getName());
        if (info == null) {
            info = new LeverInfo();
            infos.put(player.getGameProfile().getName(), info);
        }
        info.tick();
        if (info.cooldown > 0) return;
        for (int c = 0; c <= 1; c++) {
            Vec3 handPos = VRPlugin.API.getVRPlayer(player).getController(c).position();
            BlockPos handBlockPos = BlockPos.containing(handPos);
            BlockPos leverPos;
            BlockState state;
            if (player.level().getBlockState(handBlockPos).getBlock() == Blocks.LEVER) {
                state = player.level().getBlockState(handBlockPos);
                leverPos = handBlockPos;
            } else if (player.level().getBlockState(handBlockPos.below()).getBlock() == Blocks.LEVER) {
                state = player.level().getBlockState(handBlockPos.below());
                leverPos = handBlockPos.below();
            } else if (player.level().getBlockState(handBlockPos.above()).getBlock() == Blocks.LEVER) {
                state = player.level().getBlockState(handBlockPos.above());
                leverPos = handBlockPos.above();
            } else {
                continue;
            }
            Vec3 hitboxPos;
            if (state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.WALL) {
                hitboxPos = Vec3.atCenterOf(leverPos);
                Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
                Direction towardsWall = facing.getOpposite();
                Vec3 unit = Vec3.atLowerCornerOf(towardsWall.getNormal()); // Converts Vec3i to Vec3
                hitboxPos = hitboxPos.add(unit.multiply(0.25, 0.25, 0.25));
                if (state.getValue(LeverBlock.POWERED)) {
                    hitboxPos = hitboxPos.add(0, -1d/3d, 0);
                } else {
                    hitboxPos = hitboxPos.add(0, 1d/3d, 0);
                }
            } else {
                hitboxPos = Vec3.atCenterOf(leverPos);
                Direction facingOn = state.getValue(HorizontalDirectionalBlock.FACING);
                Direction facingOff = facingOn.getOpposite();
                Vec3 unit;
                if (state.getValue(LeverBlock.POWERED)) {
                    unit = Vec3.atLowerCornerOf(facingOn.getNormal());
                } else {
                    unit = Vec3.atLowerCornerOf(facingOff.getNormal());
                }
                hitboxPos = hitboxPos.add(unit.multiply(0.25, 0.25, 0.25));
            }
            double size = 0.25;
            AABB hitbox = new AABB(hitboxPos.x - size, hitboxPos.y - size, hitboxPos.z - size,
                    hitboxPos.x + size, hitboxPos.y + size, hitboxPos.z + size);
            if (hitbox.contains(handPos)) {
                Vec3 last = info.getLastPos(c);
                if (last.distanceToSqr(handPos) > 0.1*0.1) { // If our hand moved a significant amount
                    info.cooldown = 20;
                    LeverBlock leverBlock = (LeverBlock) Blocks.LEVER;
                    leverBlock.use(state, player.level(), leverPos, player, c == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                            null);
                    VRPluginProxy.rumbleIfVR_P(player, c, CommonConstants.vibrationTimeWorldInteraction);
                }
            }
            info.setLastPos(c, handPos);
        }
    }

    @Override
    protected boolean shouldTick(Player player) {
        return ActiveConfig.useLever &&
                VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player)
                && PlayerConfigs.getConfig(player).useLevers;
    }

    public static class LeverInfo {
        public int cooldown = 0;
        public Vec3 lastPosMain = Vec3.ZERO;
        public Vec3 lastPosOff = Vec3.ZERO;

        public void tick() {
            if (cooldown > 0) {
                cooldown--;
            }
        }

        public Vec3 getLastPos(int c) {
            return c == 0 ? lastPosMain : lastPosOff;
        }

        public void setLastPos(int c, Vec3 pos) {
            if (c == 0) {
                lastPosMain = pos;
            } else {
                lastPosOff = pos;
            }
        }
    }
}
