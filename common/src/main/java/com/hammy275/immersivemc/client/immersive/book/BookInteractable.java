package com.hammy275.immersivemc.client.immersive.book;

import com.hammy275.immersivemc.api.common.hitbox.OBB;
import com.hammy275.immersivemc.common.util.PosRot;

/**
 * Something that can be interacted with in a book.
 */
public interface BookInteractable {

    /**
     * @return The OBB for interaction.
     */
    public OBB getOBB();

    /**
     * Called when this interactable is hovered.
     * @param data Book data.
     * @param bookPosRot Position and rotation of the book.
     * @param other Position and rotation of the thing hovering the interactable.
     */
    public void hover(ClientBookData data, PosRot bookPosRot, PosRot other);

    /**
     * Called when this interactable is interacted with.
     * @param data Book data.
     * @param bookPosRot Position and rotation of the book.
     * @param other Position and rotation of the thing interacting with the interactable.
     */
    public void interact(ClientBookData data, PosRot bookPosRot, PosRot other);
}
