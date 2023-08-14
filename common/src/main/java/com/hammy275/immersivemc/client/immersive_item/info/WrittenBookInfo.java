package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class WrittenBookInfo extends AbstractItemInfo {
    private int leftPageIndex = 0;
    private boolean pageChanged = true;
    public FormattedText left = FormattedText.EMPTY;
    public FormattedText right = FormattedText.EMPTY;

    public WrittenBookInfo(ItemStack item, InteractionHand hand) {
        super(item, hand);
    }

    public void setLeftPageIndex(int leftPageIndex) {
        this.leftPageIndex = leftPageIndex;
        this.setPageChanged(true);
    }

    public int getLeftPageIndex() {
        return this.leftPageIndex;
    }

    public int getRightPageIndex() {
        return getLeftPageIndex() + 1;
    }

    public void setPageChanged(boolean pageChanged) {
        this.pageChanged = pageChanged;
    }

    public boolean pageChanged() {
        return this.pageChanged;
    }
}
