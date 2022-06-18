package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public class BrewingInfo extends AbstractTileEntityImmersiveInfo<BrewingStandTileEntity> {

    protected AxisAlignedBB fuelHitbox = null;
    protected AxisAlignedBB ingredientHitbox = null;
    protected AxisAlignedBB[] bottleHitboxes = new AxisAlignedBB[]{null, null, null};

    public Direction lastDir = null;

    public BrewingInfo(BrewingStandTileEntity tileEntity, int ticksToExist) {
        super(tileEntity, ticksToExist, 4);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = getAllHitboxes();
    }

    @Override
    public AxisAlignedBB getHibtox(int slot) {
        if (slot < 0 || slot > 4) {
            throw new IllegalArgumentException("Only supports slots 0-4");
        } else if (slot < 3) {
            return bottleHitboxes[slot];
        } else if (slot == 3) {
            return ingredientHitbox;
        } else {
            return fuelHitbox; // Slot 4
        }
    }

    @Override
    public AxisAlignedBB[] getAllHitboxes() {
        return new AxisAlignedBB[]
                {bottleHitboxes[0], bottleHitboxes[1], bottleHitboxes[2], ingredientHitbox, fuelHitbox};
    }

    @Override
    public void setHitbox(int slot, AxisAlignedBB hitbox) {
        if (slot < 0 || slot > 4) {
            throw new IllegalArgumentException("Only supports slots 0-4");
        } else if (slot < 3) {
            bottleHitboxes[slot] = hitbox;
        } else if (slot == 3) {
            ingredientHitbox = hitbox;
        } else {
            fuelHitbox = hitbox; // Slot 4
        }
    }

    @Override
    public boolean hasHitboxes() {
        return fuelHitbox != null;
    }
}
