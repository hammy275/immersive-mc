package com.hammy275.immersivemc.api.common.hitbox;

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
