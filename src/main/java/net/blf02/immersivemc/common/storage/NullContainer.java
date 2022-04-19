package net.blf02.immersivemc.common.storage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;

/**
 * Used so slotsChanged() calls can be ignored
 */
public class NullContainer extends Container {
    public NullContainer() {
        super(null, -1);
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return true;
    }

    @Override
    public void slotsChanged(IInventory p_75130_1_) {

    }
}
