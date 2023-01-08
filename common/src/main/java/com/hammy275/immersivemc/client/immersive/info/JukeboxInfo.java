package com.hammy275.immersivemc.client.immersive.info;


import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.AABB;

public class JukeboxInfo extends AbstractBlockEntityImmersiveInfo<JukeboxBlockEntity> {

    protected AABB discHitbox = null;

    public JukeboxInfo(JukeboxBlockEntity tileEntity, int ticksToExist) {
        super(tileEntity, ticksToExist, 0);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = new AABB[]{discHitbox};
    }

    @Override
    public AABB getHitbox(int slot) {
        return discHitbox;
    }

    @Override
    public AABB[] getAllHitboxes() {
        return new AABB[]{discHitbox};
    }

    @Override
    public void setHitbox(int slot, AABB hitbox) {
        // We can ignore the slot
        this.discHitbox = hitbox;
    }

    @Override
    public boolean hasHitboxes() {
        return discHitbox != null;
    }

    @Override
    public boolean hasItems() {
        return true; // Return true, jukeboxes don't have items
    }
}
