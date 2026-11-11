package com.hammy275.immersivemc.api.client.immersive;

import com.hammy275.immersivemc.api.common.immersive.BlockBasedImmersiveHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A functional interface for {@link BuiltBlockBasedImmersive}s for determining what should happen when a hitbox is
 * interacted with.
 * @param <E> The extra data class for this Immersive. This is the first class passed to
 *             {@link BlockBasedImmersiveBuilder#copy(BlockBasedImmersiveHandler, Class, Class, BiConsumer)}.
 */
@FunctionalInterface
public interface HitboxInteractHandler<E> {
    /**
     * Called when a hitbox is being interacted with.
     * @param info The info containing the interacted hitbox.
     * @param player The player interacting with the hitbox.
     * @param slots The slots or hitboxIndices being interacted with.
     * @param hand Which hand is interacting with the hitbox.
     * @param modifierState The state of the modifier key. See {@link ModifierPressState}.
     * @return A cooldown time. See {@link Immersive#handleHitboxInteract(ImmersiveInfo, LocalPlayer, List, InteractionHand, ModifierPressState)}
     * for more info.
     */
    int apply(BuiltBlockBasedImmersiveInfo<E> info, Player player, List<Integer> slots, InteractionHand hand, ModifierPressState modifierState);
}
