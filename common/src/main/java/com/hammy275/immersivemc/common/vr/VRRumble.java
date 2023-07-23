package com.hammy275.immersivemc.common.vr;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.DoubleControllerVibrate;
import com.hammy275.immersivemc.server.PlayerConfigs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class VRRumble {
    private static boolean rumbleInVRConfigCheck(ServerPlayer player) {
        if (player == null) {
            return ActiveConfig.doRumble;
        } else {
            return PlayerConfigs.getConfig(player).doRumble;
        }
    }

    public static void rumbleIfVR(ServerPlayer player, int controller, float rumbleDuration) {
        // Note: All rumble in ImmersiveMC should converge to this function call for config checking
        if (VRPluginVerify.hasAPI && (player == null || VRPlugin.API.playerInVR(player)) &&
            rumbleInVRConfigCheck(player)) {
            VRPlugin.API.triggerHapticPulse(controller, rumbleDuration, player);
        }
    }

    public static void rumbleIfVR_P(Player player, int controller, float rumbleDuration) {
        if (player instanceof ServerPlayer sp) {
            rumbleIfVR(sp, controller, rumbleDuration);
        } else if (player == null) {
            rumbleIfVR(null, controller, rumbleDuration);
        }
    }

    public static void doubleRumbleIfVR(ServerPlayer player, float rumbleDuration) {
        if (VRPluginVerify.hasAPI && (player == null || VRPlugin.API.playerInVR(player))) {
            if (player == null) {
                rumbleIfVR(null, 0, rumbleDuration);
                rumbleIfVR(null, 1, rumbleDuration);
            } else {
                Network.INSTANCE.sendToPlayer(player, new DoubleControllerVibrate(rumbleDuration));
            }
        }
    }

    public static void doubleRumbleIfVR_P(Player player, float rumbleDuration) {
        if (player instanceof ServerPlayer sp) {
            doubleRumbleIfVR(sp, rumbleDuration);
        } else if (player == null) {
            doubleRumbleIfVR(null, rumbleDuration);
        }
    }
}
