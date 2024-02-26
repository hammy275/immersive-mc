package com.hammy275.immersivemc.common.network;

import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import com.hammy275.immersivemc.client.immersive.info.BackpackInfo;
import com.hammy275.immersivemc.client.immersive.info.BeaconInfo;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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

    public static void handleReceiveInvData(HandlerStorage storage, BlockPos pos, ResourceLocation id) {
        Objects.requireNonNull(storage);
        Level level = Minecraft.getInstance().player.level();
        for (AbstractImmersive<?> immersive : Immersives.IMMERSIVES) {
            for (AbstractImmersiveInfo info : immersive.getTrackedObjects()) {
                if (info.getBlockPosition().equals(pos) && immersive.hasInfo(pos)) {
                    immersive.processStorageFromNetwork(info, storage);
                    return;
                } else if (info instanceof ChestInfo cInfo && cInfo.other != null && cInfo.other.getBlockPos().equals(pos)) {
                    Immersives.immersiveChest.processOtherStorageFromNetwork(info, storage);
                    return;
                }
            }
            if (immersive.getHandler() != null && immersive.getHandler().getID().equals(id) &&
                    immersive.shouldTrack(pos, level.getBlockState(pos), level.getBlockEntity(pos), level)) {
                immersive.trackObject(pos, level.getBlockState(pos), level.getBlockEntity(pos), level);
                return;
            }
        }
    }
}
