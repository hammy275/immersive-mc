package com.hammy275.immersivemc.server.immersive;

import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.network.packet.FetchAttachmentInventoryPacket;
import com.hammy275.immersivemc.common.vr.VRVerify;
import net.minecraft.server.level.ServerPlayer;

public record TrackedAttachmentImmersiveData<S extends NetworkStorage>(ServerPlayer tracker, ServerPlayer owner,
                                                                       PlayerAttachmentImmersiveHandler<S> handler) {

    public static final double maxDist = CommonConstants.distanceToRemoveAttachmentImmersive;

    public boolean shouldSync() {
        return handler.isDirtyForClientSync(tracker, owner);
    }

    public FetchAttachmentInventoryPacket<S> getSyncPacket() {
        return new FetchAttachmentInventoryPacket<>(handler, handler.makeInventoryContents(tracker, owner), owner.getUUID());
    }

    public boolean stillValid() {
        return !owner.hasDisconnected() && !tracker.hasDisconnected() && VRVerify.playerInVR(owner) &&
                owner.level() == tracker.level() &&
                tracker.distanceToSqr(owner.position()) < maxDist*maxDist;
    }


}
