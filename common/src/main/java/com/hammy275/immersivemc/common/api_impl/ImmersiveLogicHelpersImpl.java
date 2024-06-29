package com.hammy275.immersivemc.common.api_impl;

import com.hammy275.immersivemc.api.common.ImmersiveLogicHelpers;
import com.hammy275.immersivemc.client.immersive.AbstractImmersive;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.SwapPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ImmersiveLogicHelpersImpl implements ImmersiveLogicHelpers {

    public static final ImmersiveLogicHelpers INSTANCE = new ImmersiveLogicHelpersImpl();

    @Override
    public Direction getHorizontalBlockForward(Player player, BlockPos blockPos) {
        return AbstractImmersive.getForwardFromPlayer(player, blockPos);
    }

    @Override
    public void sendSwapPacket(BlockPos pos, int slot, InteractionHand hand) {
        Network.INSTANCE.sendToServer(new SwapPacket(pos, slot, hand));
    }
}
