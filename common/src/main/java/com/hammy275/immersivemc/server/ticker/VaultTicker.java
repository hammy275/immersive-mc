package com.hammy275.immersivemc.server.ticker;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.ticker.AbstractHandTicker;
import com.hammy275.immersivemc.mixin.VaultBlockEntityServerInvoker;
import com.hammy275.immersivemc.mixin.VaultSharedDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.Set;
import java.util.UUID;

public class VaultTicker extends AbstractHandTicker {
    @Override
    protected void tickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        BlockPos pos = BlockPos.containing(handData.getPos());
        ItemStack key = player.getItemInHand(hand);
        if (player.level().getBlockEntity(pos) instanceof VaultBlockEntity vault
                && VaultBlockEntityServerInvoker.immersiveMC$isValidToInsert(vault.getConfig(), key)) {
            BlockState state = vault.getBlockState();
            Set<UUID> connectedPlayers = ((VaultSharedDataAccessor) vault.getSharedData()).immersiveMC$getConnectedPlayers();
            if (connectedPlayers.contains(player.getUUID()) && state.getValue(VaultBlock.STATE) == VaultState.ACTIVE) {
                ItemInteractionResult result = state.useItemOn(player.getItemInHand(hand), player.level(), player, hand,
                        new BlockHitResult(handData.getPos(), Direction.NORTH, pos, false));
                if (result == ItemInteractionResult.SUCCESS) {
                    setCooldown(player, 8);
                }
            }
        }
    }

    @Override
    protected boolean shouldTickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        return ActiveConfig.getActiveConfigCommon(player).useTrialVaultImmersive;
    }
}
