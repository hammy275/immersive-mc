package com.hammy275.immersivemc.common.tracker;


import net.minecraft.world.entity.player.Player;

public abstract class AbstractTracker {

    protected abstract void tick(Player player);

    protected abstract boolean shouldTick(Player player);

    public void doTick(Player player) {
        if (shouldTick(player)) {
            tick(player);
        }
    }
}
