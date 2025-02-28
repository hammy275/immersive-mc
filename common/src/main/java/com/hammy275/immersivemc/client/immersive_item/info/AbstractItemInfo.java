package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class AbstractItemInfo extends AbstractHandImmersiveInfo {
    public boolean shouldRemove = false;
    public final ItemStack item;

    public AbstractItemInfo(ItemStack item, InteractionHand hand) {
        super(hand);
        this.item = item;
    }
}
