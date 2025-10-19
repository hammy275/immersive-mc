package com.hammy275.immersivemc.common.vr;

import com.hammy275.immersivemc.common.util.PosRot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPoseHistory;

public class VRUtil {

    public static PosRot posRot(VRBodyPartData data) {
        return new PosRot(data.getPos(), data.getDir(), data.getPitch(), data.getYaw(), data.getRoll());
    }

    @Nullable
    public static Vec3 changeForVelocity(Player player, VRBodyPart bodyPart) {
        VRPoseHistory history = VRAPI.instance().getHistoricalVRPoses(player);
        if (history == null || history.ticksOfHistory() < 2) return null;
        return history.netMovement(bodyPart, 2);
    }
}
