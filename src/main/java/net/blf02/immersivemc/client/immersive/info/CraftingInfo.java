package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CraftingInfo extends AbstractWorldStorageInfo implements InfoTriggerHitboxes {

    public Direction lastDir = null;

    public Vec3 outputPosition;
    public AABB outputHitbox;
    public ItemStack outputItem;

    public CraftingInfo(BlockPos pos, int ticksToExist) {
        super(pos, ticksToExist, 8); // Trigger hitbox is handled in here
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = this.hitboxes;
    }

    @Override
    public boolean hasPositions() {
        return super.hasPositions() && this.outputPosition != null;
    }

    @Override
    public boolean hasHitboxes() {
        return super.hasHitboxes() && this.outputHitbox != null;
    }

    @Override
    public AABB getTriggerHitbox(int hitboxNum) {
        return this.outputHitbox;
    }

    @Override
    public AABB[] getTriggerHitboxes() {
        return new AABB[]{this.outputHitbox};
    }
}
