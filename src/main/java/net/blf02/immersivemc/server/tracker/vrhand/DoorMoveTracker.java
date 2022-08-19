package net.blf02.immersivemc.server.tracker.vrhand;

import net.blf02.immersivemc.server.LastTickVRData;
import net.blf02.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
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
    public void preTick() {
        super.preTick();
        for (Map.Entry<String, Integer> entry : cooldown.entrySet()) {
            int newCooldown = entry.getValue() - 1;
            if (newCooldown <= 0) {
                cooldown.remove(entry.getKey());
            } else {
                cooldown.put(entry.getKey(), newCooldown);
            }
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
        BlockState blockState = getBlockStateAtHand(player, currentVRData, hand);
        Vec3 velocity = LastTickVRData.getVelocity(lastVRData.lastPlayer.getController(hand.ordinal()), currentVRData.getController(hand.ordinal()),
                lastVRData);
        Direction pushPullMainDirection = getDirectionToMove(blockState);
        // Check other direction if we're working with a fence gate
        boolean otherMoveCheck = blockState.getBlock() instanceof FenceGateBlock &&
                movingInDirectionWithThreshold(pushPullMainDirection.getOpposite(), velocity, THRESHOLD);
        if (movingInDirectionWithThreshold(pushPullMainDirection, velocity, THRESHOLD) || otherMoveCheck) {
            blockState.use(player.level, player, InteractionHand.MAIN_HAND, new BlockHitResult(
                    currentVRData.getController(hand.ordinal()).position(),
                    pushPullMainDirection, getBlockPosAtHand(currentVRData, hand), false
            ));
            cooldown.put(player.getGameProfile().getName(), 10);
        }
    }

    public static Direction getDirectionToMove(BlockState state) {
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        return isOpen ?
                state.getValue(HorizontalDirectionalBlock.FACING).getClockWise() :
                state.getValue(HorizontalDirectionalBlock.FACING);
    }
}
