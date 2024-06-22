package com.hammy275.immersivemc.common.network;

import com.hammy275.immersivemc.api.client.immersive.Immersive;
import com.hammy275.immersivemc.api.client.immersive.ImmersiveInfo;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.immersive.AbstractPlayerAttachmentImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BackpackInfo;
import com.hammy275.immersivemc.client.immersive.info.BeaconInfo;
import com.hammy275.immersivemc.client.subscribe.ClientLogicSubscriber;
import com.hammy275.immersivemc.api.common.immersive.NetworkStorage;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRRumble;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class NetworkClientHandlers {

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
                AbstractImmersiveInfo info = immersive.refreshOrTrackObject(pos, level);
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
