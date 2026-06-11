package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.SwapMode;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.server.api_impl.ItemSwapAmountImpl;
import com.hammy275.immersivemc.server.immersive.TrackedImmersives;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AttachmentSwapPacket {

    public final Identifier handlerID;
    public final UUID owner;
    public final List<Integer> slots;
    public final InteractionHand hand;
    public final SwapMode mode;

    public AttachmentSwapPacket(Identifier handlerID, UUID owner, List<Integer> slots, InteractionHand hand, SwapMode mode) {
        this.handlerID = handlerID;
        this.owner = owner;
        this.slots = slots;
        this.hand = hand;
        this.mode = mode;
    }


    public static void encode(AttachmentSwapPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeIdentifier(packet.handlerID);
        buffer.writeInt(packet.slots.size());
        for (Integer i : packet.slots) {
            buffer.writeInt(i);
        }
        buffer.writeUUID(packet.owner);
        buffer.writeInt(packet.hand == InteractionHand.MAIN_HAND ? 0 : 1);
        buffer.writeEnum(packet.mode);
    }

    public static AttachmentSwapPacket decode(RegistryFriendlyByteBuf buffer) {
        Identifier handlerID = buffer.readIdentifier();
        int size = buffer.readInt();
        List<Integer> slots = new ArrayList<>(Math.min(size, 32));
        for (int i = 0; i < size; i++) {
            slots.add(buffer.readInt());
        }
        AttachmentSwapPacket ret = new AttachmentSwapPacket(handlerID, buffer.readUUID(), slots,
                buffer.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                buffer.readEnum(SwapMode.class));
        return ret;
    }

    public static void handle(final AttachmentSwapPacket message, ServerPlayer tracker) {
        int handStackSize = tracker.getItemInHand(message.hand).getCount();
        Player ownerPlayer = tracker.level().getPlayerByUUID(message.owner);
        if (ownerPlayer instanceof ServerPlayer owner
                && tracker.distanceToSqr(owner) <= CommonConstants.distanceSquaredToRemoveAttachmentImmersive) {
            for (PlayerAttachmentImmersiveHandler<?> handler : ImmersiveHandlers.ATTACHMENT_HANDLERS) {
                if (handler.getID().equals(message.handlerID)) {
                    if (handler.enabledInConfig(tracker) && !handler.clientAuthoritative() &&
                            TrackedImmersives.getTrackedData(tracker, owner, handler).isPresent()) {
                        for (int i = 0; i < message.slots.size(); i++) {
                            ItemSwapAmount swapAmount = new ItemSwapAmountImpl(message.mode, message.slots.size(), handStackSize, i);
                            handler.swap(message.slots.get(i), message.hand, owner, tracker, swapAmount);
                        }
                    }
                    break;
                }
            }
        }

    }
}
