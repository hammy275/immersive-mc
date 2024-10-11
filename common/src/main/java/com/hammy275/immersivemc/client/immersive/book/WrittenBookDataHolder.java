package com.hammy275.immersivemc.client.immersive.book;

import net.minecraft.world.item.ItemStack;

/**
 * Something that can store book data, a book, and has a callback for when the page changes from clicking a style in
 * the book.
 */
public interface WrittenBookDataHolder {

    /**
     * @return The book data this object is holding.
     */
    public ClientBookData getData();

    /**
     * @return The book this object is holding.
     */
    public ItemStack getBook();

    /**
     * Run when the page changes from a style click.
     * @param newPage The page being changed to.
     */
    public void onPageChangeStyleClick(int newPage);
}
