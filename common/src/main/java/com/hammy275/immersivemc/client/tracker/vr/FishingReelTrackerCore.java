package com.hammy275.immersivemc.client.tracker.vr;

import com.hammy275.immersivemc.client.LastClientVRData;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ReelFishPacket;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;

public class FishingReelTrackerCore {

    public static void tick(Player player) {
        Vec3 vel = LastClientVRData.changeForVelocity(LastClientVRData.VRType.C0);
        if (vel.lengthSqr() >= 0.175 && vel.y > 0) {
            Network.INSTANCE.sendToServer(new ReelFishPacket());
        }
    }

    public static boolean shouldTick(Player player) {
        return ActiveConfig.active().useThrowingImmersive && VRPlugin.API.playerInVR(player) &&
                player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof FishingRodItem &&
                player.fishing != null;
    }
}
