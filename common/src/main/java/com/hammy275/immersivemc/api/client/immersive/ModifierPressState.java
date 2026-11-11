package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

import java.util.List;

/**
 * The state of the modifier key for handling hitbox interactions.
 * @see Immersive#handleHitboxInteract(ImmersiveInfo, LocalPlayer, List, InteractionHand, ModifierPressState)
 * @since 1.6.0-beta2
 */
public enum ModifierPressState {
    /**
     * The modifier key is being held but an interaction cannot occur without it being held, such as the user having
     * the option to only interact when the modifier key is held being enabled.
     */
    FORCE_PRESSED,
    /**
     * The modifier key is currently being held and could have deliberately not been held.
     */
    PRESSED,
    /**
     * The modifier key is currently not being held.
     */
    NOT_PRESSED;

    /**
     * @return Whether the modifier key is being held, whether the user needed to hold it to perform an interaction or
     * not.
     */
    public boolean isPhysicallyHeld() {
        return this != NOT_PRESSED;
    }

    /**
     * Gets whether the modifier key is being held under a situation where the user can choose not to press it. This is
     * identical to checking whether this {@link ModifierPressState} is {@link #PRESSED}.
     * <p>
     * Before ModifierPressState was added, most Immersives would allow for interactions without trigger press and would
     * have extra behavior when pressed. This function acts a shortcut to return to that behavior; simply replace
     * references to the {@code modifierPressed} boolean with calls to this method on the press state object.
     * @return Whether the modifier key is being held when the user could choose not to hold it.
     */
    public boolean isDeliberatelyHeld() {
        return this == PRESSED;
    }
}
