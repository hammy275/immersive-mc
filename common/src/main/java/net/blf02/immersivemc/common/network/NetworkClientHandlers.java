package net.blf02.immersivemc.common.network;

import net.blf02.immersivemc.client.immersive.AbstractBlockEntityImmersive;
import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractBlockEntityImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.BackpackInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class NetworkClientHandlers {

    public static void setBackpackOutput(ItemStack output) {
        if (Immersives.immersiveBackpack.getTrackedObjects().size() > 0) {
            BackpackInfo info = Immersives.immersiveBackpack.getTrackedObjects().get(0);
            info.craftingOutput = output;
        }
    }

    public static void handleReceiveInvData(ItemStack[] stacks, BlockPos pos) {
        Objects.requireNonNull(stacks);
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            if (singleton instanceof AbstractBlockEntityImmersive<?, ?>) {
                AbstractBlockEntityImmersive<?, ?> tileImm = (AbstractBlockEntityImmersive<?, ?>) singleton;
                for (AbstractBlockEntityImmersiveInfo<?> info : tileImm.getTrackedObjects()) {
                    if (info.getBlockPosition().equals(pos)) {
                        // Use the length of items since some mods have extra slots
                        // Like IronFurnaces has slots beyond the first 3 of a furnace
                        try {
                            for (int i = 0; i < info.items.length; i++) {
                                info.items[i] = stacks[i];
                            }
                        } catch (ArrayIndexOutOfBoundsException ignored) {}
                        return;
                    } else if (info instanceof ChestInfo) {
                        ChestInfo chestInfo = (ChestInfo) info;
                        if (chestInfo.other != null && chestInfo.other.getBlockPos().equals(pos)) {
                            for (int i = 0; i < stacks.length; i++) {
                                info.items[i + 27] = stacks[i];
                            }
                        }
                    }
                }
            }
        }
    }
}
