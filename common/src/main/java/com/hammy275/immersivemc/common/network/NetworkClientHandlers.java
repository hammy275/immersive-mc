package com.hammy275.immersivemc.common.network;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.AbstractPlayerAttachmentInfo;
import com.hammy275.immersivemc.client.immersive.info.BackpackInfo;
import com.hammy275.immersivemc.client.immersive.info.BeaconInfo;
import com.hammy275.immersivemc.client.subscribe.ClientLogicSubscriber;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandlers;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRRumble;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class NetworkClientHandlers {

    public static void checkHandlerMatch(List<ResourceLocation> serverHandlerIDs) {
        Map<ResourceLocation, ImmersiveHandler<?>> clientHandlers = new HashMap<>();
        ImmersiveHandlers.HANDLERS.forEach((handler) -> clientHandlers.put(handler.getID(), handler));

        List<ResourceLocation> serverOnly = serverHandlerIDs.stream().filter((id) -> !clientHandlers.containsKey(id)).toList();
        List<ResourceLocation> clientOnly = clientHandlers.entrySet().stream().filter((entry) ->
                !serverHandlerIDs.contains(entry.getKey()) && !entry.getValue().clientAuthoritative())
                .map(Map.Entry::getKey).toList();
        if (!serverOnly.isEmpty() || !clientOnly.isEmpty()) {
            if (!serverOnly.isEmpty()) {
                ImmersiveMC.LOGGER.error("The following Immersives were on the server, but not in your game:");
                ImmersiveMC.LOGGER.error(String.join(", ", serverOnly.stream().map(ResourceLocation::toString).toList()));
            }
            if (!clientOnly.isEmpty()) {
                ImmersiveMC.LOGGER.error("The following Immersives are in your game, but not on the server even though they're required to be:");
                ImmersiveMC.LOGGER.error(String.join(", ", clientOnly.stream().map(ResourceLocation::toString).toList()));
            }
            Set<String> missingModIDs = new HashSet<>();
            serverOnly.forEach((id) -> missingModIDs.add(id.getNamespace()));
            clientOnly.forEach((id) -> missingModIDs.add(id.getNamespace()));

            Minecraft.getInstance().getConnection().onDisconnect(new DisconnectionDetails(Component.translatable("message.immersivemc.missing_immersives",
                    String.join(", ", missingModIDs))));
        }
    }

    public static void setBeaconData(BeaconDataPacket packet) {
        for (BeaconInfo info : Immersives.immersiveBeacon.getTrackedObjects()) {
            if (packet.pos.equals(info.getBlockPosition())) {
                info.effectSelected = packet.powerIndex;
                info.regenSelected = packet.useRegen;
            }
        }
    }

    public static void setBackpackOutput(ItemStack output) {
        if (Immersives.immersiveBackpack.getTrackedObjects().size() > 0) {
            BackpackInfo info = Immersives.immersiveBackpack.getTrackedObjects().get(0);
            info.craftingOutput = output;
        }
    }

    @SuppressWarnings("unchecked")
    public static <NS extends NetworkStorage> void handleReceiveInvData(NS storage, BlockPos pos, ImmersiveHandler<NS> handler) {
        Objects.requireNonNull(storage);
        Level level = Minecraft.getInstance().player.level();
        // Search all immersives for the matching handler. If found and the block is the state we expect, create or refresh
        // the info and process storage on it.
        for (Immersive<?, ?> immersive : Immersives.IMMERSIVES) {
            if (immersive.getHandler() == handler && Util.isValidBlocks(handler, pos, level)) {
                ImmersiveInfo info = ClientLogicSubscriber.doTrackIfNotTrackingAlready(immersive, pos, level);
                if (info != null) {
                    processStorageFromNetwork(immersive, info, storage);
                }
            }
        }
        for (AbstractPlayerAttachmentImmersive<?, ?> immersive : Immersives.IMMERSIVE_ATTACHMENTS) {
            if (immersive.getHandler() == handler && immersive.shouldTrack(pos, level)) {
                AbstractPlayerAttachmentInfo info = immersive.refreshOrTrackObject(pos, level);
                if (info != null) {
                    ((AbstractPlayerAttachmentImmersive<?, NS>) immersive).processStorageFromNetwork(info, storage);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <I extends ImmersiveInfo, NS extends NetworkStorage> void processStorageFromNetwork(Immersive<?, ?> immersive,
                                                                                                       I info, NS storage) {
        Immersive<I, NS> immersiveCast = (Immersive<I, NS>) immersive;
        immersiveCast.processStorageFromNetwork(info, storage);
    }

    public static void doDoubleRumble(float duration) {
        VRRumble.doubleRumbleIfVR(Minecraft.getInstance().player, duration);
    }
}
