package com.hammy275.immersivemc.server;

import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.data.VRPose;

public class ServerVRSubscriber {

    public static void vrPlayerTick(ServerPlayer player) {
        VRPose vrPose = VR.API.getVRPose(player);
        if (vrPose != null) {
            double dist = CommonConstants.registerImmersivePickRange;
            Vec3 start = vrPose.getHead().getPos();
            Vec3 look = vrPose.getHead().getDir();
            Vec3 end = vrPose.getHead().getPos().add(look.x * dist, look.y * dist, look.z * dist);
            BlockHitResult blockHit = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                    player));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                TrackedImmersives.maybeTrackImmersive(player, blockHit.getBlockPos());
            }
        }
    }

}
