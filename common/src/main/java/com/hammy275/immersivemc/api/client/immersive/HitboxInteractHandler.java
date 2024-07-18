package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface HitboxInteractHandler<E> {
    /**
     * Called when a hitbox is being interacted with.
     * @param info The info containing the interacted hitbox.
     * @param player The player interacting with the hitbox.
     * @param slot The slot or hitboxIndex being interacted with.
     * @param hand Which hand is interacting with the hitbox.
     * @return A cooldown time. See {@link Immersive#handleHitboxInteract(ImmersiveInfo, LocalPlayer, int, InteractionHand)}
     * for more info.
     */
    int apply(BuiltImmersiveInfo<E> info, Player player, int slot, InteractionHand hand);
}
