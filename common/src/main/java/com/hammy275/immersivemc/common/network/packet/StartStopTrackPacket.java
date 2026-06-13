package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record StartStopTrackPacket(ResourceLocation handlerID, boolean isStop) {

    public static void encode(StartStopTrackPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.handlerID).writeBoolean(packet.isStop);
    }

    public static StartStopTrackPacket decode(RegistryFriendlyByteBuf buffer) {
        return new StartStopTrackPacket(buffer.readResourceLocation(), buffer.readBoolean());
    }

    public static void handle(final StartStopTrackPacket packet, ServerPlayer player) {
        if (player != null) {
            PlayerAttachmentImmersiveHandler<?> handler = ImmersiveHandlers.ATTACHMENT_HANDLERS.stream()
                    .filter(h -> h.getID().equals(packet.handlerID))
                    .findAny().orElse(null);
            if (handler != null) {
                if (packet.isStop) {
                    TrackedImmersives.stopTracking(handler, player);
                } else if (VRVerify.playerInVR(player)) {
                    TrackedImmersives.maybeTrackImmersive(player, player, handler);
                }
            }
        }
    }
}
