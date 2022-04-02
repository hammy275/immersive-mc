package net.blf02.immersivemc.common.network;

import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.AbstractTileEntityImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AbstractTileEntityImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.ChestInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class NetworkClientHandlers {

    public static void handleReceiveInvData(ItemStack[] stacks, BlockPos pos) {
        Objects.requireNonNull(stacks);
        for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
            if (singleton instanceof AbstractTileEntityImmersive<?, ?>) {
                AbstractTileEntityImmersive<?, ?> tileImm = (AbstractTileEntityImmersive<?, ?>) singleton;
                for (AbstractTileEntityImmersiveInfo<?> info : tileImm.getTrackedObjects()) {
                    if (info.getBlockPosition().equals(pos)) {
                        for (int i = 0; i < stacks.length; i++) {
                            info.items[i] = stacks[i];
                        }
                        return;
                    } else if (info instanceof ChestInfo) {
                        ChestInfo chestInfo = (ChestInfo) info;
                        if (chestInfo.other.getBlockPos().equals(pos)) {
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
