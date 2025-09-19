package com.hammy275.immersivemc.common.subscribe;

import net.minecraft.world.entity.player.Player;

public class CommonSubscriber {

    public static void globalTick(Object ignored) {
        // Note: Unsure if this is run double on the hosts of singleplayer worlds.
    }

    public static void onPlayerTick(Player player) {

    }
}
