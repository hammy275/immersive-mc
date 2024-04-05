package com.hammy275.immersivemc.common.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Used so slotsChanged() calls can be ignored
 */
public class NullContainer extends AbstractContainerMenu {
    public NullContainer() {
        super(null, -1);
    }

    @Override
    public boolean stillValid(Player p_75145_1_) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void slotsChanged(Container p_75130_1_) {

    }
}
