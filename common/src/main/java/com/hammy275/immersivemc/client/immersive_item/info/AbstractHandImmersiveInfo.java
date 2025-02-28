package com.hammy275.immersivemc.client.immersive_item.info;

import net.minecraft.world.InteractionHand;

public class AbstractHandImmersiveInfo {
    public boolean shouldRemove = false;
    public InteractionHand hand;

    public AbstractHandImmersiveInfo(InteractionHand hand) {
        this.hand = hand;
    }
}
