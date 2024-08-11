package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.util.PageChangeState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;

public class BookData implements NetworkStorage {

    public ItemStack book = ItemStack.EMPTY;
    public int leftPageIndex = 0;
    public PageChangeState pageChangeState = PageChangeState.NONE;
    public float leftPageTurn = 0f;
    public float rightPageTurn = 1f;

    private boolean pageChanged = false;


    public void setPage(int newPageIndex) {
        if (newPageIndex % 2 != 0) {
            newPageIndex--;
        }
        if (newPageIndex > maxLeftPageIndex()) {
            newPageIndex = maxLeftPageIndex();
        } else if (newPageIndex < 0) {
            newPageIndex = 0;
        }
        leftPageIndex = newPageIndex;
    }

    public void nextPage() {
        setPage(leftPageIndex + 2);
    }

    public void lastPage() {
        setPage(leftPageIndex - 2);
    }

    public boolean isPageChanged() {
        return pageChanged;
    }

    public void markPageNoLongerChanged() {
        pageChanged = false;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItem(book).writeInt(leftPageIndex).writeEnum(pageChangeState)
                .writeFloat(leftPageTurn).writeFloat(rightPageTurn);

    }

    @Override
    public void decode(FriendlyByteBuf buffer) {
        this.book = buffer.readItem();
        this.leftPageIndex = buffer.readInt();
        this.pageChangeState = buffer.readEnum(PageChangeState.class);
        this.leftPageTurn = buffer.readFloat();
        this.rightPageTurn = buffer.readFloat();
    }

    // Note: Current behavior assumes you can't remove pages from the book.
    protected int maxLeftPageIndex() {
        return getPageCount() % 2 == 0 ? getPageCount() - 2 : getPageCount() - 1;
    }

    protected int getPageCount() {
        if (book.isEmpty()) return 0;
        return WrittenBookItem.getPageCount(book);
    }
}
