package com.hammy275.immersivemc.client.immersive.book;

import com.hammy275.immersivemc.common.util.PosRot;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

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

    /**
     * Called to get the offset on the page to start rendering from. +X is towards the book center, +Y is towards
     * the top of the page, and +Z is towards the player's face if they're looking directly at the book.
     * <br>
     * The coordinate system is described by example below. The rest should be pretty inferrable.
     * <ul>
     *     <li>The center of the page is located at (0, 0, 0)</li>
     *     <li>(1, 0, 0) is the very center of the book</li>
     *     <li>(0, 1, 0) is the top-center of the page</li>
     *     <li>(0, 0, 1) is an arbitrary unit closer to the player's face.</li>
     * </ul>
     * @param data The book data being rendered.
     * @param leftPage Whether this is rendering on the left page or the right.
     * @param bookPosRot The book's position and rotation.
     * @return The offset to use as described above.
     */
    public Vec3 getStartOffset(ClientBookData data, boolean leftPage, PosRot bookPosRot);
}
