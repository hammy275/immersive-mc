package com.hammy275.immersivemc.client.ticker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ReelFishPacket;
import com.hammy275.immersivemc.common.ticker.AbstractTicker;
import com.hammy275.immersivemc.common.vr.VR;
import com.hammy275.immersivemc.common.vr.VRUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

public class FishingReelTicker extends AbstractTicker {

    @Override
    protected void tick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        Vec3 vel = VRUtil.changeForVelocity(player, VRBodyPart.fromInteractionHand(InteractionHand.MAIN_HAND));
        if (vel.lengthSqr() >= 0.175 && vel.y > 0) {
            Network.INSTANCE.sendToServer(new ReelFishPacket());
        }
    }

    @Override
    protected boolean shouldTick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        return ActiveConfig.active().useThrowingImmersive && VR.API.isVRPlayer(player) &&
                player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem &&
                player.fishing != null;
    }
}
