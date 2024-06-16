package com.hammy275.immersivemc.api.common.hitbox;

/**
 * An object representing one hitbox for an {@link com.hammy275.immersivemc.api.client.immersive.Immersive}. You
 * may obtain instances of this interface using {@link HitboxInfoFactory}. You could also implement this yourself,
 * but that is NOT covered under the API, so API changes may result in your implementations of this interface breaking
 * without warning.
 */
public interface HitboxInfo {

    /**
     * @return Get the actual hitbox used for collision, intersection, etc.
     */
    public BoundingBox getHitbox();

    /**
     * @return Whether this hitbox is a trigger hitbox. As of writing, this is if the break block button is
     *         required to be held while a hand is in this hitbox to activate it in VR.
     */
    public boolean isTriggerHitbox();
}
