package com.hammy275.immersivemc.server.tracker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.ticker.AbstractTicker;
import com.hammy275.immersivemc.common.vr.VRRumble;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.*;

public class RangedGrabTickerServer extends AbstractTicker {

    public static final RangedGrabTickerServer INSTANCE = new RangedGrabTickerServer();

    public static final double moveMultiplier = 2d/3d;

    public final Map<UUID, RangedGrabInfo> infos = new HashMap<>();

    @Override
    protected void tick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        RangedGrabInfo info = infos.get(player.getUUID()); // Won't be null, since shouldTick() ensures one exists
        if (info.item == null || !info.item.isAlive() || info.tickTime <= 0 || !ActiveConfig.getConfigForPlayer(player).useRangedGrabImmersive) {
            infos.remove(player.getUUID());
        } else {
            info.tickTime--;
            info.item.setPickUpDelay(0);
            Vec3 baseVelocity = new Vec3(0, 0, 0);
            if (info.tickTime > 35) {
                baseVelocity = baseVelocity.add(0, 0.25, 0);
            }
            info.item.lookAt(EntityAnchorArgument.Anchor.EYES, player.position().add(0, 1, 0));
            Vec3 move = info.item.getLookAngle().multiply(moveMultiplier, moveMultiplier, moveMultiplier).add(baseVelocity);
            info.item.setDeltaMovement(move.x, move.y, move.z);
            info.item.hurtMarked = true; // velocityChanged from MCP
            VRRumble.rumbleIfVR(player, InteractionHand.MAIN_HAND, CommonConstants.vibrationTimeRangedGrab);
        }
    }

    @Override
    protected boolean shouldTick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        return ActiveConfig.FILE_SERVER.useRangedGrabImmersive && infos.get(player.getUUID()) != null;
    }

    public static class RangedGrabInfo {

        public final ItemEntity item;
        public int tickTime = 40;

        public RangedGrabInfo(ItemEntity item, ServerPlayer player) {
            this.item = item;
        }
    }
}
