package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.mixin.AbstractCauldronBlockAccessor;
import com.hammy275.immersivemc.mixin.BucketItemAccessor;
import com.hammy275.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CauldronTracker extends AbstractVRHandTracker {

    private Map<UUID, Integer> cooldown = new HashMap<>();

    @Override
    public void preTick(Player player) {
        super.preTick(player);
        int newCooldown = cooldown.getOrDefault(player.getUUID(), 0) - 1;
        if (newCooldown <= 0) {
            cooldown.remove(player.getUUID());
        } else {
            cooldown.put(player.getUUID(), newCooldown);
        }
    }

    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        if (cooldown.getOrDefault(player.getUUID(), 0) > 0) return false;
        IVRData data = currentVRData.getController(hand.ordinal());
        // If block at hand pos is cauldron or block at hand pos is air and block below is cauldron.
        return player.level.getBlockState(new BlockPos(data.position())).getBlock() instanceof AbstractCauldronBlock ||
                (player.level.getBlockState(new BlockPos(data.position()).below()).getBlock() instanceof AbstractCauldronBlock
                && player.level.getBlockState(new BlockPos(data.position())).isAir());
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        IVRData data = currentVRData.getController(hand.ordinal());
        BlockState handState = player.level.getBlockState(new BlockPos(data.position()));

        BlockPos cauldronPos = handState.getBlock() instanceof AbstractCauldronBlock ? new BlockPos(data.position()) :
                new BlockPos(data.position()).below();
        BlockState cauldron = player.level.getBlockState(cauldronPos);
        AbstractCauldronBlock cauldronBlock = (AbstractCauldronBlock) cauldron.getBlock();
        boolean inCauldronBlock = handState.getBlock() instanceof AbstractCauldronBlock;
        ItemStack handStack = player.getItemInHand(hand);
        Item handItem = handStack.getItem();
        Potion heldPotion = PotionUtils.getPotion(handStack);
        CauldronInteraction interaction = ((AbstractCauldronBlockAccessor) cauldronBlock).getInteractions().get(handItem);

        if (interaction == null) return;

        // If holding an empty bucket or glass bottle, see if we can fill it.
        if (inCauldronBlock && (handItem instanceof BottleItem || (handItem instanceof BucketItem bucketItem && ((BucketItemAccessor) bucketItem).getFluid().isSame(Fluids.EMPTY)))) {
            // Pointing up in any way
            if (Math.abs(data.getRoll()) < 90) {
                possiblySetCooldown(player, interaction.interact(cauldron, player.level, cauldronPos, player, hand, handStack));
            }
        } else if ((handItem instanceof PotionItem && heldPotion == Potions.WATER) ||
                (handItem instanceof BucketItem bucketItem && !((BucketItemAccessor) bucketItem).getFluid().isSame(Fluids.EMPTY)) ||
                handItem instanceof SolidBucketItem) {
            // 20-degrees in either direction from straight down
            if (Math.abs(data.getRoll()) > 160) {
                possiblySetCooldown(player, interaction.interact(cauldron, player.level, cauldronPos, player, hand, handStack));
            }
        } else if (inCauldronBlock) {
            possiblySetCooldown(player, interaction.interact(cauldron, player.level, cauldronPos, player, hand, handStack));
        }


    }

    @Override
    public boolean isEnabledInConfig(ActiveConfig config) {
        return config.useCauldronImmersive;
    }

    private void possiblySetCooldown(Player player, InteractionResult res) {
        if (res.consumesAction()) {
            cooldown.put(player.getUUID(), 5);
        }
    }
}
