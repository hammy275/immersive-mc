package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.mixin.AbstractCauldronBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

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
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, VRPose currentVRPose) {
        if (cooldown.getOrDefault(player.getUUID(), 0) > 0) return false;
        VRBodyPartData data = currentVRPose.getHand(hand);
        // If block at hand pos is cauldron or block at hand pos is air and block below is cauldron.
        return player.level().getBlockState(BlockPos.containing(data.getPos())).getBlock() instanceof AbstractCauldronBlock ||
                (player.level().getBlockState(BlockPos.containing(data.getPos()).below()).getBlock() instanceof AbstractCauldronBlock
                && player.level().getBlockState(BlockPos.containing(data.getPos())).isAir());
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, VRPose currentVRData) {
        VRBodyPartData data = currentVRData.getHand(hand);
        BlockState handState = player.level().getBlockState(BlockPos.containing(data.getPos()));

        BlockPos cauldronPos = handState.getBlock() instanceof AbstractCauldronBlock ? BlockPos.containing(data.getPos()) :
                BlockPos.containing(data.getPos()).below();
        BlockState cauldron = player.level().getBlockState(cauldronPos);
        AbstractCauldronBlock cauldronBlock = (AbstractCauldronBlock) cauldron.getBlock();
        boolean inCauldronBlock = handState.getBlock() instanceof AbstractCauldronBlock;
        ItemStack handStack = player.getItemInHand(hand);
        Item handItem = handStack.getItem();
        PotionContents contents = handStack.get(DataComponents.POTION_CONTENTS);
        Holder<Potion> heldPotion = contents == null ? null : contents.potion().orElse(null);
        CauldronInteraction interaction = ((AbstractCauldronBlockAccessor) cauldronBlock).immersiveMC$getInteractions().map().get(handItem);

        if (interaction == null) return;

        // If holding an empty bucket or glass bottle, see if we can fill it.
        if (inCauldronBlock && (handItem instanceof BottleItem || (handItem instanceof BucketItem bucketItem && Platform.getFluid(bucketItem).isSame(Fluids.EMPTY)))) {
            // Pointing up in any way
            if (Math.abs(data.getRoll()) < Math.PI / 2) {
                possiblySetCooldown(player, interaction.interact(cauldron, player.level(), cauldronPos, player, hand, handStack));
            }
        } else if ((handItem instanceof PotionItem && heldPotion == Potions.WATER) ||
                (handItem instanceof BucketItem bucketItem && !Platform.getFluid(bucketItem).isSame(Fluids.EMPTY)) ||
                handItem instanceof SolidBucketItem) {
            // 20-degrees in either direction from straight down
            if (Math.abs(data.getRoll()) > Math.PI - Math.toRadians(20)) {
                possiblySetCooldown(player, interaction.interact(cauldron, player.level(), cauldronPos, player, hand, handStack));
            }
        } else if (inCauldronBlock) {
            possiblySetCooldown(player, interaction.interact(cauldron, player.level(), cauldronPos, player, hand, handStack));
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
