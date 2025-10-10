package com.hammy275.immersivemc.client.tracker.vr;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ReelFishPacket;
import com.hammy275.immersivemc.common.vr.VRUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPart;

public class FishingReelTrackerCore {

    public static void tick(Player player) {
        Vec3 vel = VRUtil.changeForVelocity(player, VRBodyPart.fromInteractionHand(InteractionHand.MAIN_HAND));
        if (vel.lengthSqr() >= 0.175 && vel.y > 0) {
            Network.INSTANCE.sendToServer(new ReelFishPacket());
        }
    }

    public static boolean shouldTick(Player player) {
        return ActiveConfig.active().useThrowingImmersive && VRAPI.instance().isVRPlayer(player) &&
                player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem &&
                player.fishing != null;
    }
}
