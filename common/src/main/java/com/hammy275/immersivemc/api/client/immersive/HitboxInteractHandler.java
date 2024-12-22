package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.List;

@FunctionalInterface
public interface HitboxInteractHandler<E> {
    /**
     * Called when a hitbox is being interacted with.
     * @param info The info containing the interacted hitbox.
     * @param player The player interacting with the hitbox.
     * @param slots The slots or hitboxIndices being interacted with.
     * @param hand Which hand is interacting with the hitbox.
     * @return A cooldown time. See {@link Immersive#handleHitboxInteract(ImmersiveInfo, LocalPlayer, List, InteractionHand)}
     * for more info.
     */
    int apply(BuiltImmersiveInfo<E> info, Player player, List<Integer> slots, InteractionHand hand);
}
