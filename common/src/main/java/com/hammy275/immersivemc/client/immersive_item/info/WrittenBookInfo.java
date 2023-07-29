package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class WrittenBookInfo extends AbstractItemInfo {
    public int leftPageNum = 1;

    public WrittenBookInfo(ItemStack item, InteractionHand hand) {
        super(item, hand);
    }
}
