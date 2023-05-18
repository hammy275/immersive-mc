package com.hammy275.immersivemc.client.immersive.info;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Arrays;

public class SmithingTableInfo extends AbstractWorldStorageInfo {
    public Direction renderDirection;
    public Direction lastDir = null;


    public SmithingTableInfo(BlockPos pos, int ticksToExist) {
        super(pos, ticksToExist, 2);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = Arrays.copyOfRange(hitboxes, 0, 2);
    }

    @Override
    public boolean readyToRender() {
        return this.hasPositions() && this.hasHitboxes()
                 && this.renderDirection != null;
    }
}
