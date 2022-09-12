package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class AnvilInfo extends AbstractWorldStorageInfo {
    public Direction renderDirection;
    public boolean isReallyAnvil;

    public Vec3 textPos = null;
    public Direction lastDir = null;

    public int anvilCost = 0;

    public AnvilInfo(BlockPos pos, int ticksToExist) {
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
