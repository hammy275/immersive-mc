package net.blf02.immersivemc.common.compat.vr;

import net.minecraft.entity.player.PlayerEntity;

public class VRStatus {

    public static boolean hasVR = false;

    public boolean shouldVR(PlayerEntity player) {
        return hasVR && VRPlugin.API.playerInVR(player);
    }

}
