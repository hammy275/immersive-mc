package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class AbstractItemInfo {
    public boolean shouldRemove = false;
    public InteractionHand handIn;
    public final ItemStack item;

    public AbstractItemInfo(ItemStack item, InteractionHand hand) {
        this.item = item;
        this.handIn = hand;
    }
}
