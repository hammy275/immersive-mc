package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.client.config.ClientConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class BeaconInfo extends AbstractWorldStorageInfo implements InfoTriggerHitboxes {

    public boolean regenSelected = true;
    public int effectSelected = -1;
    public Direction lastPlayerDir = null;
    /**
     * Breakdown:
     * Index 0-4 hold the 5 effects. Can contain nulls!
     * Index 5 holds the regen hitbox. Can be null!
     * Index 6 holds the + hitbox. Can be null!
     */
    public AABB[] triggerBoxes = new AABB[7];
    public Vec3 effectSelectedDisplayPos = null;
    public BeaconInfo(BlockPos pos) {
        super(pos, ClientConstants.ticksToRenderBeacon, 0);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = Arrays.copyOfRange(hitboxes, 0, 1);
    }

    @Override
    public boolean readyToRender() {
        return super.readyToRender() && lastPlayerDir != null
                && triggerBoxes[1] != null && effectSelectedDisplayPos != null;
    }

    @Override
    public AABB getTriggerHitbox(int hitboxNum) {
        return triggerBoxes[hitboxNum];
    }

    @Override
    public AABB[] getTriggerHitboxes() {
        return triggerBoxes;
    }
}
