package com.hammy275.immersivemc.client.immersive.info;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;

public class SmithingTableInfo extends AbstractWorldStorageInfo implements InfoTriggerHitboxes {
    public Direction renderDirection;
    public Direction lastDir = null;

    public SmithingTableInfo(BlockPos pos, int ticksToExist) {
        super(pos, ticksToExist, 3);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = Arrays.copyOfRange(hitboxes, 0, 3);
    }

    @Override
    public boolean readyToRender() {
        return this.hasPositions() && this.hasHitboxes()
                 && this.renderDirection != null;
    }

    @Override
    public AABB getTriggerHitbox(int hitboxNum) {
        return this.hitboxes[3];
    }

    @Override
    public AABB[] getTriggerHitboxes() {
        return new AABB[]{this.hitboxes[3]};
    }

    @Override
    public int getVRControllerNum() {
        return 0;
    }
}
