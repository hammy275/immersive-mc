package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.client.immersive.info.BuiltImmersiveInfo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface RightClickHandler {
    void apply(BuiltImmersiveInfo info, Player player, int slot, InteractionHand hand);
}
