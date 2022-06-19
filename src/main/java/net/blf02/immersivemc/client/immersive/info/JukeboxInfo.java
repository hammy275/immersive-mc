package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class JukeboxInfo extends AbstractTileEntityImmersiveInfo<JukeboxTileEntity> {

    protected AxisAlignedBB discHitbox = null;

    public JukeboxInfo(JukeboxTileEntity tileEntity, int ticksToExist) {
        super(tileEntity, ticksToExist, 0);
    }

    @Override
    public void setInputSlots() {
        this.inputHitboxes = new AxisAlignedBB[]{discHitbox};
    }

    @Override
    public AxisAlignedBB getHitbox(int slot) {
        return discHitbox;
    }

    @Override
    public AxisAlignedBB[] getAllHitboxes() {
        return new AxisAlignedBB[]{discHitbox};
    }

    @Override
    public void setHitbox(int slot, AxisAlignedBB hitbox) {
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
