package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class WrittenBookInfo extends AbstractItemInfo {
    private int leftPageIndex = 0;
    private boolean pageChanged = true;
    public FormattedText left = FormattedText.EMPTY;
    public FormattedText right = FormattedText.EMPTY;
    public AABB prevHitbox = null;
    public AABB nextHitbox = null;
    public AABB centerHitbox = null;
    public AABB bookHitbox = null;

    private final int maxLeftPageIndex;

    public WrittenBookInfo(ItemStack item, InteractionHand hand) {
        super(item, hand);
        BookViewScreen.WrittenBookAccess access = new BookViewScreen.WrittenBookAccess(item);
        maxLeftPageIndex = (access.getPageCount() - 1) / 2;
    }

    public void nextPage() {
        if (leftPageIndex + 2 <= maxLeftPageIndex) {
            leftPageIndex += 2;
        }
        pageChanged = true;
    }

    public void lastPage() {
        if (leftPageIndex - 2 >= 0) {
            leftPageIndex -= 2;
        }
        pageChanged = true;
    }

    public boolean onFirstPage() {
        return leftPageIndex == 0;
    }

    public boolean onLastPage() {
        return leftPageIndex == maxLeftPageIndex;
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
