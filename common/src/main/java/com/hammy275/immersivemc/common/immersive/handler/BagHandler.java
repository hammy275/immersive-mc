package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.BagStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class BagHandler implements PlayerAttachmentImmersiveHandler<BagStorage> {
    @Override
    public BagStorage makeInventoryContents(ServerPlayer tracker, ServerPlayer owner) {
        return new BagStorage(ImmersiveMCPlayerStorages.getBackpackCraftingStorage(owner),
                ActiveConfig.getActiveConfigCommon(owner));
    }

    @Override
    public BagStorage getEmptyNetworkStorage() {
        return new BagStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, ServerPlayer owner, ServerPlayer tracker, ItemSwapAmount amount) {
        if (tracker == owner) { // Only owner should be able to modify contents of bag
            if (slot < 27) {
                Swap.handleInventorySwap(owner, slot + 9, InteractionHand.MAIN_HAND);
            } else {
                Swap.handleBackpackCraftingSwap(slot - 27, hand,
                        ImmersiveMCPlayerStorages.getBackpackCraftingStorage(owner), owner, amount);
                DirtyTracker.dirtyBagStorages.add(owner);
            }
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer tracker, ServerPlayer owner) {
        return DirtyTracker.dirtyBagStorages.contains(owner);
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useBagImmersive;
    }

    @Override
    public boolean clientAuthoritative() {
        return false;
    }

    @Override
    public Identifier getID() {
        return Util.id("bag");
    }
}
