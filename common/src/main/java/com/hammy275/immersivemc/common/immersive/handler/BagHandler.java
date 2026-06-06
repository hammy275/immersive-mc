package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BagHandler implements PlayerAttachmentImmersiveHandler<ListOfItemsStorage> {
    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer tracker, ServerPlayer owner) {
        ListOfItemsStorage storage = new ListOfItemsStorage();
        // Only players can see their bag contents
        if (tracker == owner) {
            for (int i = 9; i < 36; i++) {
                storage.getItems().add(owner.getInventory().getItem(i));
            }
        } else {
            for (int i = 0; i < 27; i++) {
                storage.getItems().add(ItemStack.EMPTY);
            }
        }
        // But everyone can see crafting contents, since those around you see you hold the item if you're crafting with
        // it, so no need to hide anything.
        storage.getItems().addAll(ImmersiveMCPlayerStorages.getBackpackCraftingStorage(owner));
        return storage;
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, ServerPlayer owner, ServerPlayer tracker, ItemSwapAmount amount) {
        if (tracker == owner) {
            if (slot < 27) {
                Swap.handleInventorySwap(owner, slot, InteractionHand.MAIN_HAND);
            } else {
                Swap.handleBackpackCraftingSwap(slot - 27, hand,
                        ImmersiveMCPlayerStorages.getBackpackCraftingStorage(owner), owner, amount);
            }
        }
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer tracker, ServerPlayer owner) {
        return false;
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
