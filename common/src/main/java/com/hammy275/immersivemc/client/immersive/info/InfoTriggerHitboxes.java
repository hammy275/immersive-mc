package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import net.minecraft.world.InteractionHand;

/**
 * Attached to immersive infos to signify that they have hitboxes that should only activate
 * when holding the trigger while in VR.
 */
public interface InfoTriggerHitboxes {

    public BoundingBox getTriggerHitbox(int hitboxNum);

    public BoundingBox[] getTriggerHitboxes();

    // InfoTriggerHitboxes must define which controller number can interact with them
    public InteractionHand getVRHand();
}
