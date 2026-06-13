package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.api.common.immersive.ItemSwapAmount;
import com.hammy275.immersivemc.api.common.immersive.PlayerAttachmentImmersiveHandler;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class HitboxesHandler implements PlayerAttachmentImmersiveHandler<NullStorage> {
    @Override
    public NullStorage makeInventoryContents(ServerPlayer tracker, ServerPlayer owner) {
        return new NullStorage();
    }

    @Override
    public NullStorage getEmptyNetworkStorage() {
        return new NullStorage();
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
        return true; // Always enabled
    }

    @Override
    public boolean clientAuthoritative() {
        return true;
    }

    @Override
    public Identifier getID() {
        return Util.id("hitboxes");
    }
}
