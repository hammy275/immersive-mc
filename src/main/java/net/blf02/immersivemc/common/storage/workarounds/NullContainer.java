package net.blf02.immersivemc.common.storage.workarounds;

import net.minecraft.entity.player.Player;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.container.Container;

/**
 * Used so slotsChanged() calls can be ignored
 */
public class NullContainer extends Container {
    public NullContainer() {
        super(null, -1);
    }

    @Override
    public boolean stillValid(Player p_75145_1_) {
        return true;
    }

    @Override
    public void slotsChanged(Container p_75130_1_) {

    }
}
