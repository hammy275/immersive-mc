package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.client.immersive.PlayerAttachmentImmersive;
import com.hammy275.immersivemc.api.client.immersive.PlayerAttachmentImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.Immersives;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Iterator;
import java.util.UUID;

public record StopTrackPacketWithOwner(ResourceLocation handlerID, UUID owner) {

    public static void encode(StopTrackPacketWithOwner packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.handlerID).writeUUID(packet.owner);
    }

    public static StopTrackPacketWithOwner decode(FriendlyByteBuf buffer) {
        return new StopTrackPacketWithOwner(buffer.readResourceLocation(), buffer.readUUID());
    }

    public static void handle(final StopTrackPacketWithOwner packet, ServerPlayer player) {
        if (player == null) {
            PlayerAttachmentImmersive<?, ?, ?> immersive = Immersives.ATTACHMENT_IMMERSIVES.stream()
                    .filter(i -> i.getHandler().getID().equals(packet.handlerID))
                    .findAny().orElse(null);
            if (immersive != null) {
                removeFirst(immersive, packet.owner);
            }
        }
    }

    private static <I extends PlayerAttachmentImmersiveInfo> void removeFirst(PlayerAttachmentImmersive<I, ?, ?> immersive, UUID owner) {
        Iterator<I> iterator = immersive.getTrackedObjects().iterator();
        while (iterator.hasNext()) {
            I info = iterator.next();
            if (info.getOwner().getUUID().equals(owner)) {
                iterator.remove();
                break;
            }
        }
    }
}
