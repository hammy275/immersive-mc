package com.hammy275.immersivemc.common.network;

import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BackpackInfo;
import com.hammy275.immersivemc.client.immersive.info.BeaconInfo;
import com.hammy275.immersivemc.common.immersive.handler.ImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.network.NetworkStorage;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
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

    public static void handleReceiveInvData(NetworkStorage storage, BlockPos pos, ImmersiveHandler handler) {
        Objects.requireNonNull(storage);
        Level level = Minecraft.getInstance().player.level;
        // Search all immersives for the matching handler. If found and the block is the state we expect, create or refresh
        // the info and process storage on it.
        for (AbstractImmersive<?> immersive : Immersives.IMMERSIVES) {
            if (immersive.getHandler() == handler && immersive.shouldTrack(pos, level)) {
                AbstractImmersiveInfo info = immersive.refreshOrTrackObject(pos, level);
                if (info != null) {
                    immersive.processStorageFromNetwork(info, storage);
                }
            }
        }
    }

    public static void doDoubleRumble(float duration) {
        VRRumble.doubleRumbleIfVR(Minecraft.getInstance().player, duration);
    }
}
