package net.blf02.immersivemc.common.tracker;

import net.minecraft.entity.player.PlayerEntity;

public abstract class AbstractTracker {

    protected abstract void tick(PlayerEntity player);

    protected abstract boolean shouldTick(PlayerEntity player);

    public void doTick(PlayerEntity player) {
        if (shouldTick(player)) {
            tick(player);
        }
    }
}
