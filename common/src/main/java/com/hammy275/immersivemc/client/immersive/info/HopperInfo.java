package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.config.ClientConstants;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;

public class HopperInfo extends AbstractBlockEntityImmersiveInfo<HopperBlockEntity> {

    protected AABB[] hitboxes = new AABB[5];
    public Direction lastDirectionForBoxPos = null;
    public Direction lastDirectionForBoxRot = null;

    public HopperInfo(HopperBlockEntity hopper) {
        super(hopper, ClientConstants.ticksToRenderHopper, 4);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = this.hitboxes;
    }

    @Override
    public AABB getHitbox(int slot) {
        return this.hitboxes[slot];
    }

    @Override
    public AABB[] getAllHitboxes() {
        return this.hitboxes;
    }

    @Override
    public void setHitbox(int slot, AABB hitbox) {
        this.hitboxes[slot] = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return this.hitboxes[4] != null;
    }
}
