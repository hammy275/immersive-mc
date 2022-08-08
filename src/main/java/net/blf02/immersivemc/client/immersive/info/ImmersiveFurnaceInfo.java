package net.blf02.immersivemc.client.immersive.info;


import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.AABB;

public class ImmersiveFurnaceInfo extends AbstractBlockEntityImmersiveInfo<AbstractFurnaceBlockEntity> {

    protected AABB toSmeltHitbox = null;
    protected AABB fuelHitbox = null;
    protected AABB outputHitbox = null;
    public final Direction forward;

    public ImmersiveFurnaceInfo(AbstractFurnaceBlockEntity furnace, int ticksLeft) {
        super(furnace, ticksLeft, 2);
        this.forward = furnace.getBlockState().getValue(AbstractFurnaceBlock.FACING);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = new AABB[]{this.toSmeltHitbox, this.fuelHitbox};
    }

    public AABB getHitbox(int slot) {
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

    public AABB[] getAllHitboxes() {
        return new AABB[]{toSmeltHitbox, fuelHitbox, outputHitbox};
    }

    public void setHitbox(int slot, AABB hitbox) {
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
