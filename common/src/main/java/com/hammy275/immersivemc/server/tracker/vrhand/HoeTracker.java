package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.common.config.ServerPlayerConfig;
import com.hammy275.immersivemc.server.LastTickVRData;
import com.hammy275.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HoeTracker extends AbstractVRHandTracker {

    public static final double THRESHOLD = 0.0575;

    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        return stackInHand.getItem() instanceof HoeItem;
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        IVRData handDataCurrent = currentVRData.getController(hand.ordinal());
        IVRData handDataLast = lastVRData.lastPlayer.getController(hand.ordinal());
        if (LastTickVRData.getAllVelocity(handDataLast, handDataCurrent, lastVRData) >= THRESHOLD) {
            Vec3 hit = handDataCurrent.position().add(handDataCurrent.getLookAngle().multiply(1d/3d, 1d/3d, 1d/3d));
            BlockPos pos = new BlockPos(hit);
            if (!handleTill(player, hand, pos)) {
                handleHarvest(player, pos, hand);
            }

        }
    }

    protected boolean handleTill(Player player, InteractionHand hand, BlockPos pos) {
        InteractionResult result = player.getItemInHand(hand).getItem().useOn(new UseOnContext(player, hand,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.DOWN, pos, false)));
        boolean didTill = result == InteractionResult.CONSUME || result == InteractionResult.SUCCESS;
        if (didTill) {
            // Need to play sound separately since useOn code expects the client to do it
            player.level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        this.damageHoe(player, hand);
        return didTill;
    }

    protected void handleHarvest(Player player, BlockPos pos, InteractionHand hand) {
        ItemStack hoe = player.getItemInHand(hand);
        BlockState state = player.level.getBlockState(pos);
        if (state.getBlock() == Blocks.WHEAT && state.getValue(CropBlock.AGE) == CropBlock.MAX_AGE) {
            // Can cast to ServerLevel here since trackers are only run server-side
            List<ItemStack> drops = Block.getDrops(state, (ServerLevel) player.level,
                    pos, null, player, hoe);
            for (ItemStack d : drops) {
                if (d.getItem() == Items.WHEAT_SEEDS) {
                    d.shrink(1);
                }
                if (!d.isEmpty()) {
                    Vec3 dropPos = Vec3.atCenterOf(pos);
                    ItemEntity drop = new ItemEntity(player.level, dropPos.x, dropPos.y, dropPos.z, d);
                    player.level.addFreshEntity(drop);
                }
                state = state.setValue(CropBlock.AGE, 0);
                player.level.setBlock(pos, state, 2);
            }
            this.damageHoe(player, hand);
        }
    }

    private void damageHoe(Player player, InteractionHand hand) {
        player.getItemInHand(hand).hurtAndBreak(1, player, (playerCallback) -> player.broadcastBreakEvent(hand));
    }


    @Override
    public boolean isEnabledInConfig(ServerPlayerConfig config) {
        return config.useHoeImmersion;
    }
}
