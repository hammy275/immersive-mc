package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.common.api_impl.hitbox.OBBImpl;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin into AABB to add asOBB() and asAABB() implementation methods for BoundingBox.
 * These are not appended with immersiveMC$ to fit BoundingBox's contract.
 */
@Mixin(AABB.class)
public abstract class AABBBoundingBoxImplMixin implements BoundingBox {
    @Override
    public OBBImpl asOBB() {
        throw new RuntimeException("Cannot get OBB as AABB!");
    }

    @Override
    public AABB asAABB() {
        return (AABB) (Object) this;
    }
}
