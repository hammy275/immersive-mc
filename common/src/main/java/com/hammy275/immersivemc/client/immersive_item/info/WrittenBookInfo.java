package com.hammy275.immersivemc.client.immersive_item.info;

import com.hammy275.immersivemc.client.immersive.book.ClientBookData;
import com.hammy275.immersivemc.client.immersive.book.WrittenBookDataHolder;
import com.hammy275.immersivemc.client.immersive.book.WrittenBookHelpers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class WrittenBookInfo extends AbstractItemInfo implements WrittenBookDataHolder {

    public ClientBookData bookData;
    public int light = -1;
    public boolean didClick = false;

    public WrittenBookInfo(ItemStack item, InteractionHand hand) {
        super(item, hand);
        bookData = WrittenBookHelpers.makeClientBookData(this);
    }

    @Override
    public ClientBookData getData() {
        return this.bookData;
    }

    @Override
    public ItemStack getBook() {
        return this.item;
    }

    @Override
    public void onPageChangeStyleClick(int newPage) {
        didClick = true;
    }
}
