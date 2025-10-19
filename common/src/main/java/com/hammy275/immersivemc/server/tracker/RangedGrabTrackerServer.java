package com.hammy275.immersivemc.server.tracker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.vr.VRRumble;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RangedGrabTrackerServer extends AbstractTracker {

    public static final double moveMultiplier = 2d/3d;

    public final List<RangedGrabInfo> infos = new ArrayList<>();

    protected final List<RangedGrabInfo> toRemove = new LinkedList<>();

    public RangedGrabTrackerServer() {
        ServerTrackerInit.globalTrackers.add(this);
    }

    @Override
    protected void tick(Player player) {
        for (RangedGrabInfo info : infos) {
            if (info.item == null || !info.item.isAlive() || info.tickTime <= 0) {
                toRemove.add(info);
            } else {
                if (!ActiveConfig.getConfigForPlayer(info.player).useRangedGrabImmersive) return;
                info.tickTime--;
                info.item.setPickUpDelay(0);
                Vec3 baseVelocity = new Vec3(0, 0, 0);
                if (info.tickTime > 35) {
                    baseVelocity = baseVelocity.add(0, 0.25, 0);
                }
                info.item.lookAt(EntityAnchorArgument.Anchor.EYES, info.player.position().add(0, 1, 0));
                Vec3 move = info.item.getLookAngle().multiply(moveMultiplier, moveMultiplier, moveMultiplier).add(baseVelocity);
                info.item.setDeltaMovement(move.x, move.y, move.z);
                info.item.hurtMarked = true; // velocityChanged from MCP
                VRRumble.rumbleIfVR(info.player, InteractionHand.MAIN_HAND, CommonConstants.vibrationTimeRangedGrab);
            }
        }

        for (RangedGrabInfo toRem : toRemove) {
            infos.remove(toRem);
        }
    }

    @Override
    protected boolean shouldTick(Player player) {
        return ActiveConfig.FILE_SERVER.useRangedGrabImmersive && infos.size() > 0;
    }

    public static class RangedGrabInfo {

        public final ItemEntity item;
        public final ServerPlayer player;
        public int tickTime = 40;

        public RangedGrabInfo(ItemEntity item, ServerPlayer player) {
            this.item = item;
            this.player = player;
        }
    }
}
