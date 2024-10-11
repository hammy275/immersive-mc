package com.hammy275.immersivemc.client.immersive.book;

import com.hammy275.immersivemc.common.util.PosRot;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Something that can be rendered in a book.
 */
public interface BookRenderable {

    /**
     * Called to render this.
     * @param stack The PoseStack to use with rendering. This is already centered on the page and rotated appropriately.
     * @param data The book data being rendered.
     * @param leftPage Whether this is rendering on the left page or the right.
     * @param light Light value.
     * @param bookPosRot The book's position and rotation.
     */
    public void render(PoseStack stack, ClientBookData data, boolean leftPage, int light, PosRot bookPosRot);
}
