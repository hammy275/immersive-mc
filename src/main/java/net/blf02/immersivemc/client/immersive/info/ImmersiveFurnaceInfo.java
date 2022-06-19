package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public class ImmersiveFurnaceInfo extends AbstractTileEntityImmersiveInfo<AbstractFurnaceTileEntity> {

    protected AxisAlignedBB toSmeltHitbox = null;
    protected AxisAlignedBB fuelHitbox = null;
    protected AxisAlignedBB outputHitbox = null;
    public final Direction forward;

    public ImmersiveFurnaceInfo(AbstractFurnaceTileEntity furnace, int ticksLeft) {
        super(furnace, ticksLeft, 2);
        this.forward = furnace.getBlockState().getValue(AbstractFurnaceBlock.FACING);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = new AxisAlignedBB[]{this.toSmeltHitbox, this.fuelHitbox};
    }

    public AxisAlignedBB getHitbox(int slot) {
        switch (slot) {
            case 0:
                return toSmeltHitbox;
            case 1:
                return fuelHitbox;
            case 2:
                return outputHitbox;
        }
        throw new IllegalArgumentException("Only has slots 0 to 2.");
    }

    public AxisAlignedBB[] getAllHitboxes() {
        return new AxisAlignedBB[]{toSmeltHitbox, fuelHitbox, outputHitbox};
    }

    public void setHitbox(int slot, AxisAlignedBB hitbox) {
        switch (slot) {
            case 0:
                this.toSmeltHitbox = hitbox;
                break;
            case 1:
                this.fuelHitbox = hitbox;
                break;
            case 2:
                this.outputHitbox = hitbox;
                break;
            default:
                throw new IllegalArgumentException("Only has slots 0 to 2.");
        }
    }

    public boolean hasHitboxes() {
        // Fuel hitbox is always set, let's check that one
        return fuelHitbox != null;
    }

    @Override
    public boolean hasPositions() {
        return positions[1] != null;
    }
}
