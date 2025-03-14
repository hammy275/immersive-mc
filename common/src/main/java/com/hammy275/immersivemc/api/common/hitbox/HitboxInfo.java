package com.hammy275.immersivemc.api.common.hitbox;

/**
 * An object representing one hitbox for an {@link com.hammy275.immersivemc.api.client.immersive.Immersive}. You
 * may obtain instances of this interface using {@link HitboxInfoFactory}, though if your hitbox moves, you'll
 * likely want to create an implementation of this yourself so you can properly implement
 * {@link #getRenderHitbox(float)}. Although not in the API,
 * {@link com.hammy275.immersivemc.client.immersive.info.HitboxItemPair} is a good example of an implementation of
 * this interface.
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

    /**
     * @param partialTick The partial tick between the current tick and the next.
     * @return The bounding box to be used for rendering.
     */
    public BoundingBox getRenderHitbox(float partialTick);
}
