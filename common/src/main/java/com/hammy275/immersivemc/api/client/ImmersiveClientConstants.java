package com.hammy275.immersivemc.api.client;

import com.hammy275.immersivemc.client.api_impl.ImmersiveClientConstantsImpl;

/**
 * Contains functions to retrieve constants used by ImmersiveMC. The values themselves may change between ImmersiveMC
 * versions, but they are guaranteed to always be constant. You should use the return values from these methods rather
 * than hardcoding the cooldown as a number, since ImmersiveMC may change them in the future!
 */
public interface ImmersiveClientConstants {

    /**
     * @return An ImmersiveClientConstants instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static ImmersiveClientConstants instance() {
        return ImmersiveClientConstantsImpl.INSTANCE;
    }

    /**
     * @return The default non-VR cooldown for Immersives in ticks.
     */
    public int defaultCooldown();

    /**
     * @return The time, in ticks, for items in Immersives to grow to full size.
     */
    public int growTransitionTime();

    /**
     * @return The multiplier to a rendered item's size when it is hovered over (pointed at by desktop players or
     * containing the hand in VR).
     */
    public float hoverScaleMultiplier();

}
