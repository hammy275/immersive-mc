package com.hammy275.immersivemc.common.vr;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.DoubleControllerVibrate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.server.VRServerAPI;

public class VRRumble {
    private static boolean rumbleInVRConfigCheck(Player player) {
        if (player instanceof ServerPlayer) {
            return ActiveConfig.getConfigForPlayer(player).doVRControllerRumble;
        } else {
            return ActiveConfig.FILE_CLIENT.doVRControllerRumble;
        }
    }

    public static void rumbleIfVR(Player player, InteractionHand hand, float rumbleDuration) {
        // Note: All rumble in ImmersiveMC should converge to this function call for config checking
        if (VRVerify.playerInVR(player) && rumbleInVRConfigCheck(player)) {
            if (player instanceof ServerPlayer sp) {
                VRServerAPI.instance().sendHapticPulse(sp, VRBodyPart.fromInteractionHand(hand), rumbleDuration);
            } else if (player.isLocalPlayer()) {
                VRClientAPI.instance().triggerHapticPulse(VRBodyPart.fromInteractionHand(hand), rumbleDuration);
            }
        }
    }

    public static void doubleRumbleIfVR(Player player, float rumbleDuration) {
        if (VRVerify.playerInVR(player)) {
            if (player instanceof ServerPlayer sp) {
                Network.INSTANCE.sendToPlayer(sp, new DoubleControllerVibrate(rumbleDuration));
            } else {
                rumbleIfVR(player, InteractionHand.MAIN_HAND, rumbleDuration);
                rumbleIfVR(player, InteractionHand.OFF_HAND, rumbleDuration);
            }
        }
    }
}
