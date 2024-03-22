package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.mixin.DoorBlockMixin;
import com.hammy275.immersivemc.mixin.FenceGateBlockMixin;
import com.hammy275.immersivemc.server.LastTickVRData;
import com.hammy275.immersivemc.server.data.LastTickData;
import dev.architectury.platform.Platform;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
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
import java.util.concurrent.ThreadLocalRandom;

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

            // Need to play the sound separately for the player opening/closing the block
            if (res == InteractionResult.CONSUME) {
                VRRumble.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimeWorldInteraction);
                boolean isNowOpen = blockState.getValue(BlockStateProperties.OPEN);
                SoundEvent sound = null;
                if (blockState.getBlock() instanceof FenceGateBlock fence) {
                    // Forge uses the old SoundEvent system. Haven't found a good way to deal with that.
                    if (!Platform.isForge()) {
                        FenceGateBlockMixin accessor = (FenceGateBlockMixin) fence;
                        sound = isNowOpen ? accessor.getType().fenceGateOpen() : accessor.getType().fenceGateClose();
                    }
                } else if (blockState.getBlock() instanceof DoorBlock door) {
                    DoorBlockMixin accessor = (DoorBlockMixin) door;
                    sound = isNowOpen ? accessor.getType().doorOpen() : accessor.getType().doorClose();
                }
                if (sound != null && player instanceof ServerPlayer sPlayer) {
                    sPlayer.connection.send(new ClientboundSoundPacket(
                            Holder.direct(sound),
                            SoundSource.BLOCKS,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            1f, ThreadLocalRandom.current().nextFloat() * 0.1F + 0.9F,
                            ThreadLocalRandom.current().nextLong()));
                }
            }

            cooldown.put(player.getGameProfile().getName(), 10);
        }
    }

    @Override
    public boolean isEnabledInConfig(ActiveConfig config) {
        return config.useDoorImmersion;
    }

    public static Direction getDirectionToMove(BlockState state) {
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        return isOpen ?
                state.getValue(HorizontalDirectionalBlock.FACING).getClockWise() :
                state.getValue(HorizontalDirectionalBlock.FACING);
    }
}
