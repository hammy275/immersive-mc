package com.hammy275.immersivemc.api.common.hitbox;

import com.hammy275.immersivemc.common.api_impl.hitbox.HitboxInfoFactoryImpl;

/**
 * A class containing methods to create {@link HitboxInfo} instances.
 */
public interface HitboxInfoFactory {

    /**
     * @return A HitboxInfoBuilder instance to access API functions. All details of this object not
     *         mentioned in this file are assumed to be an implementation detail, and may change at any time.
     */
    public static HitboxInfoFactory instance() {
        return HitboxInfoFactoryImpl.INSTANCE;
    }

    /**
     * Creates a hitbox that, by default, is interacted with by right-click outside of VR and by placing a hand inside
     * the hitbox in VR.
     *
     * @param boundingBox The BoundingBox representing the hitbox.
     * @return A HitboxInfo for use in Immersives.
     */
    public HitboxInfo interactHitbox(BoundingBox boundingBox);

    /**
     * Creates a hitbox that, by default, is interacted with by right-click outside of VR and by pressing the button
     * bound to block breaking while a hand is inside the hitbox in VR.
     * @param boundingBox The BoundingBox representing the hitbox.
     * @return A HitboxInfo for use in Immersives.
     */
    public HitboxInfo triggerHitbox(BoundingBox boundingBox);
}
