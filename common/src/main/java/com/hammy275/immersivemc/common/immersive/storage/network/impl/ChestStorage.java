package com.hammy275.immersivemc.common.immersive.storage.network.impl;

import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Storage for syncing chest contents. Only the openness and isDirty are kept on the server continuously, and the
 * isDirty only denotes if the lid handling is dirty, not the chest as a whole.
 * <br>
 * During syncing, there are generally two instances, one is the instance stored in the server, and the other
 * is the instance created with the items to be sent to clients.
 */
public class ChestStorage extends ListOfItemsStorage {

    private float openness = -1f;
    private boolean isDirty = false;

    public ChestStorage() {
    }

    public ChestStorage(ListOfItemsStorage itemsStorage) {
        super(itemsStorage.getItems());
    }

    public ChestStorage(ListOfItemsStorage itemsStorage, ChestStorage lidStorage) {
        super(itemsStorage.getItems());
        this.openness = lidStorage.getOpenness();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(openness);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buffer) {
        this.openness = buffer.readFloat();
    }

    public float getOpenness() {
        return openness;
    }

    public void setOpenness(float openness) {
        this.openness = openness;
        isDirty = true;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setNoLongerDirty() {
        isDirty = false;
    }
}
