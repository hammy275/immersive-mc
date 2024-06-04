package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface HitboxInteractHandler<E> {
    int apply(BuiltImmersiveInfo<E> info, Player player, int slot, InteractionHand hand);
}
