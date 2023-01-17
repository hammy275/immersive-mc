package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.common.config.ServerPlayerConfig;
import com.hammy275.immersivemc.mixin.DoorBlockMixin;
import com.hammy275.immersivemc.server.LastTickVRData;
import com.hammy275.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class DoorMoveTracker extends AbstractVRHandTracker {

    public static final double THRESHOLD = 0.06;

    public Map<String, Integer> cooldown = new HashMap<>();

    @Override
    public void preTick(Player player) {
        super.preTick(player);
        int newCooldown = cooldown.getOrDefault(player.getGameProfile().getName(), 0) - 1;
        if (newCooldown <= 0) {
            cooldown.remove(player.getGameProfile().getName());
        } else {
            cooldown.put(player.getGameProfile().getName(), newCooldown);
        }
    }

    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        Block block = getBlockAtHand(player, currentVRData, hand);
        return cooldown.get(player.getGameProfile().getName()) == null &&
                (block instanceof FenceGateBlock || block instanceof DoorBlock);
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        BlockPos pos = getBlockPosAtHand(currentVRData, hand);
        BlockState blockState = player.level.getBlockState(pos);
        Vec3 velocity = LastTickVRData.getVelocity(lastVRData.lastPlayer.getController(hand.ordinal()), currentVRData.getController(hand.ordinal()),
                lastVRData);
        Direction pushPullMainDirection = getDirectionToMove(blockState);
        // Check other direction if we're working with a fence gate
        boolean otherMoveCheck = blockState.getBlock() instanceof FenceGateBlock &&
                movingInDirectionWithThreshold(pushPullMainDirection.getOpposite(), velocity, THRESHOLD);
        if (movingInDirectionWithThreshold(pushPullMainDirection, velocity, THRESHOLD) || otherMoveCheck) {
            InteractionResult res = blockState.use(player.level, player, InteractionHand.MAIN_HAND, new BlockHitResult(
                    currentVRData.getController(hand.ordinal()).position(),
                    pushPullMainDirection, getBlockPosAtHand(currentVRData, hand), false
            ));

            // Play event to door opener/closer, since the use() call ignores them
            if (res == InteractionResult.CONSUME) {
                boolean isNowOpen = blockState.getValue(BlockStateProperties.OPEN);
                int event = -1;
                if (blockState.getBlock() instanceof DoorBlock door) {
                    DoorBlockMixin accessor = (DoorBlockMixin) door;
                    event = isNowOpen ? accessor.openSound() : accessor.closeSound();
                } else {
                    event = isNowOpen ? 1008 : 1014; // Hardcoded into FenceGateBlock
                }

                if (event != -1 && player instanceof ServerPlayer sPlayer) {
                    sPlayer.connection.send(new ClientboundLevelEventPacket(
                            event, pos, 0, false
                    ));
                }
            }



            cooldown.put(player.getGameProfile().getName(), 10);
        }
    }

    @Override
    public boolean isEnabledInConfig(ServerPlayerConfig config) {
        return config.useDoorImmersion;
    }

    public static Direction getDirectionToMove(BlockState state) {
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        return isOpen ?
                state.getValue(HorizontalDirectionalBlock.FACING).getClockWise() :
                state.getValue(HorizontalDirectionalBlock.FACING);
    }
}
