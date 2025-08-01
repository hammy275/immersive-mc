package com.hammy275.immersivemc.server.api_impl;

import com.hammy275.immersivemc.api.common.immersive.SwapResult;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record SwapResultImpl(ItemStack playerHandStack, ItemStack immersiveStack, ItemStack leftoverStack)
        implements SwapResult {

    @Override
    public void giveToPlayer(Player player, InteractionHand hand) {
        player.setItemInHand(hand, playerHandStack);
        Util.placeLeftovers(player, leftoverStack);
    }
}
