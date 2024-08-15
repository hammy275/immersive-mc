package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class WrittenBookInfo extends AbstractItemInfo {

    public ClientBookData bookData;

    public WrittenBookInfo(ItemStack item, InteractionHand hand) {
        super(item, hand);
        bookData = new ClientBookData(true, item);
    }
}
