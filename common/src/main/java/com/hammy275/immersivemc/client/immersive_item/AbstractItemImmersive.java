package com.hammy275.immersivemc.client.immersive_item;

import com.hammy275.immersivemc.client.immersive_item.info.AbstractItemInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractItemImmersive<I extends AbstractItemInfo> extends AbstractHandImmersive<I> {

    public abstract boolean itemMatches(ItemStack item);

    protected abstract I createInfo(ItemStack item, InteractionHand hand);

    @Override
    protected I createInfo(InteractionHand hand) {
        return createInfo(Minecraft.getInstance().player.getItemInHand(hand), hand);
    }

    @Override
    public boolean activeForHand(InteractionHand hand) {
        ItemStack stack = Minecraft.getInstance().player.getItemInHand(hand);
        return !stack.isEmpty() && itemMatches(stack);
    }

    @Override
    protected boolean handSwapCandidate(InteractionHand newHand) {
        return activeForHand(newHand);
    }
}
