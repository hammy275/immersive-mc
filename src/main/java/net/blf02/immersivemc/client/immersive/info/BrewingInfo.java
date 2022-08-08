package net.blf02.immersivemc.client.immersive.info;


import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.phys.AABB;

public class BrewingInfo extends AbstractBlockEntityImmersiveInfo<BrewingStandBlockEntity> {

    protected AABB fuelHitbox = null;
    protected AABB ingredientHitbox = null;
    protected AABB[] bottleHitboxes = new AABB[]{null, null, null};

    public Direction lastDir = null;

    public BrewingInfo(BrewingStandBlockEntity tileEntity, int ticksToExist) {
        super(tileEntity, ticksToExist, 4);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = getAllHitboxes();
    }

    @Override
    public AABB getHitbox(int slot) {
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
    public AABB[] getAllHitboxes() {
        return new AABB[]
                {bottleHitboxes[0], bottleHitboxes[1], bottleHitboxes[2], ingredientHitbox, fuelHitbox};
    }

    @Override
    public void setHitbox(int slot, AABB hitbox) {
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
