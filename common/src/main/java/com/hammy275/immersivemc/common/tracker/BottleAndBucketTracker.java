package com.hammy275.immersivemc.common.tracker;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRVerify;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRPose;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BottleAndBucketTracker extends AbstractTracker {

    private final Map<UUID, Integer> cooldown = new HashMap<>();

    @Override
    protected void tick(Player player) {
        for (int c = 0; c <= 1; c++) {
            InteractionHand hand = InteractionHand.values()[c];
            ItemStack stackInHand = player.getItemInHand(hand);
            if (stackMatches(stackInHand)) {
                VRPose currentVRPose = VRAPI.instance().getVRPose(player);
                if (Math.abs(currentVRPose.getHand(hand).getRoll()) < Math.PI / 2) {
                    boolean holdingGlassBottle = stackInHand.is(Items.GLASS_BOTTLE);
                    BlockPos pos = BlockPos.containing(currentVRPose.getHand(hand).getPos());
                    BlockState state = player.level().getBlockState(pos);
                    BucketPickup pickup = state.getBlock() instanceof BucketPickup bp ? bp : null;
                    boolean isWaterSource = state.is(Blocks.WATER) && state.getValue(BlockStateProperties.LEVEL) == 0;
                    if ((holdingGlassBottle && isWaterSource) || pickup != null) {
                        possiblyPlaceItemAndSetCooldown(player, hand, Util.doUse(player, hand, pos));
                    }
                }
            }
        }
    }

    @Override
    protected boolean shouldTick(Player player) {
        if (!VRVerify.playerInVR(player)) return false;
        if (!ActiveConfig.getActiveConfigCommon(player).useBucketAndBottleImmersive) return false;
        int newCooldown = cooldown.getOrDefault(player.getUUID(), 0) - 1;
        if (newCooldown <= 0) {
            cooldown.remove(player.getUUID());
        } else {
            cooldown.put(player.getUUID(), newCooldown);
            return false;
        }
        return true;
    }

    private boolean stackMatches(ItemStack stackInHand) {
        return stackInHand.is(Items.GLASS_BOTTLE) ||
                stackInHand.getItem() instanceof BucketItem bucketItem && Platform.getFluid(bucketItem).isSame(Fluids.EMPTY);
    }


    private void possiblyPlaceItemAndSetCooldown(Player player, InteractionHand hand, InteractionResult res) {
        if (res.consumesAction() && res instanceof InteractionResult.Success success) {
            if (success.heldItemTransformedTo() != null) {
                cooldown.put(player.getUUID(), 5);
                player.setItemInHand(hand, success.heldItemTransformedTo());
            }
        }
    }
}
