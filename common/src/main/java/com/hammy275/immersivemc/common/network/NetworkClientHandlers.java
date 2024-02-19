package com.hammy275.immersivemc.common.network;

import com.hammy275.immersivemc.client.immersive.AbstractBlockEntityImmersive;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.client.immersive.BuiltImmersive;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.*;
import com.hammy275.immersivemc.common.immersive.storage.HandlerStorage;
import com.hammy275.immersivemc.common.immersive.storage.ListOfItemsStorage;
import com.hammy275.immersivemc.common.network.packet.BeaconDataPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
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
        if (id.getPath().equals("generic")) {
            List<ItemStack> stacks = ((ListOfItemsStorage) storage).getItems();
            for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                if (singleton instanceof AbstractBlockEntityImmersive<?, ?>) {
                    AbstractBlockEntityImmersive<?, ?> tileImm = (AbstractBlockEntityImmersive<?, ?>) singleton;
                    for (AbstractBlockEntityImmersiveInfo<?> info : tileImm.getTrackedObjects()) {
                        if (info.getBlockPosition().equals(pos)) {
                            // Use the length of items since some mods have extra slots
                            // Like IronFurnaces has slots beyond the first 3 of a furnace
                            try {
                                for (int i = 0; i < info.items.length; i++) {
                                    info.items[i] = stacks.get(i);
                                }
                            } catch (ArrayIndexOutOfBoundsException ignored) {}
                            return;
                        } else if (info instanceof ChestInfo) {
                            ChestInfo chestInfo = (ChestInfo) info;
                            if (chestInfo.other != null && chestInfo.other.getBlockPos().equals(pos)) {
                                for (int i = 0; i < stacks.size(); i++) {
                                    info.items[i + 27] = stacks.get(i);
                                }
                            }
                        }
                    }
                } else if (singleton instanceof BuiltImmersive builtImmersive) {
                    for (BuiltImmersiveInfo info : builtImmersive.getTrackedObjects()) {
                        if (info.getBlockPosition().equals(pos)) {
                            for (int i = 0; i < info.itemHitboxes.size(); i++) {
                                info.itemHitboxes.get(i).item = stacks.get(i);
                            }
                        }
                    }
                }
            }
        } else {
            for (AbstractImmersive<?> immersive : Immersives.IMMERSIVES) {
                for (AbstractImmersiveInfo info : immersive.getTrackedObjects()) {
                    if (info.getBlockPosition().equals(pos)) {
                        immersive.processStorageFromNetwork(info, storage);
                    }
                }
            }
        }
    }
}
