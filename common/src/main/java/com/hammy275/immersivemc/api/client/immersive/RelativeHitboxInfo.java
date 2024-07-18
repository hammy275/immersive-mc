package com.hammy275.immersivemc.api.client.immersive;

import net.minecraft.world.phys.Vec3;

/**
 * A built relative hitbox. These are used with {@link ImmersiveBuilder}s, placed relative to a center defined by
 * the builder's selected {@link HitboxPositioningMode}, and can potentially hold items. They can be created using
 * {@link RelativeHitboxInfoBuilder}s.
 */
public interface RelativeHitboxInfo {

    /**
     * Create a clone of this relative hitbox, but add the provided offset to the offset of the clone.
     * @param offset The offset to add.
     * @return A clone of this relative hitbox with the changes described above.
     */
    public RelativeHitboxInfo cloneWithAddedOffset(Vec3 offset);

    /**
     * @return A clone of this relative hitbox in builder form.
     */
    public RelativeHitboxInfoBuilder getBuilderClone();
}
