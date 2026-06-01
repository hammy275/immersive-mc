package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ListOfItemsStorage;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class BagHandler implements PlayerAttachmentImmersiveHandler<ListOfItemsStorage> {
    @Override
    public ListOfItemsStorage makeInventoryContents(ServerPlayer tracker, ServerPlayer owner) {
        return new ListOfItemsStorage();
    }

    @Override
    public ListOfItemsStorage getEmptyNetworkStorage() {
        return new ListOfItemsStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, ServerPlayer owner, ServerPlayer tracker, ItemSwapAmount amount) {

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
        // TODO: Server-authoritative so we can do syncing
        return true;
    }

    @Override
    public Identifier getID() {
        return Util.id("bag");
    }
}
