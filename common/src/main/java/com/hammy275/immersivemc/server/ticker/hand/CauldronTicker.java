package com.hammy275.immersivemc.server.ticker.hand;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.ticker.AbstractHandTicker;
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
import org.vivecraft.api.data.VRPoseHistory;

public class CauldronTicker extends AbstractHandTicker {

    @Override
    protected void tickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        BlockState handState = player.level().getBlockState(BlockPos.containing(handData.getPos()));

        BlockPos cauldronPos = handState.getBlock() instanceof AbstractCauldronBlock ? BlockPos.containing(handData.getPos()) :
                BlockPos.containing(handData.getPos()).below();
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
            if (Math.abs(handData.getRoll()) < Math.PI / 2) {
                possiblySetCooldown(player, interaction.interact(cauldron, player.level(), cauldronPos, player, hand, handStack));
            }
        } else if ((handItem instanceof PotionItem && heldPotion == Potions.WATER) ||
                (handItem instanceof BucketItem bucketItem && !Platform.getFluid(bucketItem).isSame(Fluids.EMPTY)) ||
                handItem instanceof SolidBucketItem) {
            // 20-degrees in either direction from straight down
            if (Math.abs(handData.getRoll()) > Math.PI - Math.toRadians(20)) {
                possiblySetCooldown(player, interaction.interact(cauldron, player.level(), cauldronPos, player, hand, handStack));
            }
        } else if (inCauldronBlock) {
            possiblySetCooldown(player, interaction.interact(cauldron, player.level(), cauldronPos, player, hand, handStack));
        }
    }

    @Override
    protected boolean shouldTickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        return ActiveConfig.getConfigForPlayer(player).useCauldronImmersive && (
                // If block at hand pos is cauldron or block at hand pos is air and block below is cauldron.
                player.level().getBlockState(BlockPos.containing(handData.getPos())).getBlock() instanceof AbstractCauldronBlock ||
                        (player.level().getBlockState(BlockPos.containing(handData.getPos()).below()).getBlock() instanceof AbstractCauldronBlock
                                && player.level().getBlockState(BlockPos.containing(handData.getPos())).isAir())
                );
    }

    private void possiblySetCooldown(Player player, InteractionResult res) {
        if (res.consumesAction()) {
            setCooldown(player, 5);
        }
    }
}
