package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class ImmersiveFurnaceInfo extends AbstractImmersiveInfo<AbstractFurnaceTileEntity> {

    protected AxisAlignedBB toSmeltHitbox = null;
    protected AxisAlignedBB fuelHitbox = null;
    protected AxisAlignedBB outputHitbox = null;

    public ImmersiveFurnaceInfo(AbstractFurnaceTileEntity furnace, int ticksLeft) {
        super(furnace, 0, 2, ticksLeft);
    }

    public AxisAlignedBB getHibtox(int slot) {
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
        // If we have one hitbox, we have all 3
        return toSmeltHitbox != null;
    }
}
