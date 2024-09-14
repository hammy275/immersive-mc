package com.hammy275.immersivemc.common.tracker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.mixin.BucketItemAccessor;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

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
                IVRPlayer currentVRData = VRPlugin.API.getVRPlayer(player);
                if (Math.abs(currentVRData.getController(hand.ordinal()).getRoll()) < 90) {
                    boolean holdingGlassBottle = stackInHand.is(Items.GLASS_BOTTLE);
                    BlockPos pos = BlockPos.containing(currentVRData.getController(hand.ordinal()).position());
                    BlockState state = player.level.getBlockState(pos);
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
        if (!VRPluginVerify.playerInVR(player)) return false;
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
                stackInHand.getItem() instanceof BucketItem bucketItem && ((BucketItemAccessor) bucketItem).getFluid().isSame(Fluids.EMPTY);
    }


    private void possiblyPlaceItemAndSetCooldown(Player player, InteractionHand hand, InteractionResultHolder<ItemStack> res) {
        if (res.getResult().consumesAction() && !res.getObject().isEmpty()) {
            cooldown.put(player.getUUID(), 5);
            player.setItemInHand(hand, res.getObject());
        }
    }
}
