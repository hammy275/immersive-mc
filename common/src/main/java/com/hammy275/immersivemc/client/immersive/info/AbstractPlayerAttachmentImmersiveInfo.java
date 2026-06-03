package com.hammy275.immersivemc.client.immersive.info;

import com.hammy275.immersivemc.api.client.immersive.PlayerAttachmentImmersiveInfo;
import net.minecraft.client.player.AbstractClientPlayer;

public class AbstractPlayerAttachmentImmersiveInfo extends AbstractImmersiveInfo implements PlayerAttachmentImmersiveInfo {

    protected final AbstractClientPlayer owner;

    public AbstractPlayerAttachmentImmersiveInfo(AbstractClientPlayer owner) {
        this.owner = owner;
    }

    @Override
    public AbstractClientPlayer getOwner() {
        return owner;
    }
}
