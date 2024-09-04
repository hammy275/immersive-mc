package com.hammy275.immersivemc.common.vr;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.DoubleControllerVibrate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class VRRumble {
    private static boolean rumbleInVRConfigCheck(Player player) {
        if (player instanceof ServerPlayer) {
            return ActiveConfig.getConfigForPlayer(player).doVRControllerRumble;
        } else {
            return ActiveConfig.FILE_CLIENT.doVRControllerRumble;
        }
    }

    public static void rumbleIfVR(Player player, int controller, float rumbleDuration) {
        // Note: All rumble in ImmersiveMC should converge to this function call for config checking
        if (VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player) &&
            rumbleInVRConfigCheck(player)) {
            VRPlugin.API.triggerHapticPulse(controller, rumbleDuration, player instanceof ServerPlayer sp ? sp : null);
        }
    }

    public static void doubleRumbleIfVR(Player player, float rumbleDuration) {
        if (VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(player)) {
            if (player instanceof ServerPlayer sp) {
                Network.INSTANCE.sendToPlayer(sp, new DoubleControllerVibrate(rumbleDuration));
            } else {
                rumbleIfVR(player, 0, rumbleDuration);
                rumbleIfVR(player, 1, rumbleDuration);
            }
        }
    }
}
