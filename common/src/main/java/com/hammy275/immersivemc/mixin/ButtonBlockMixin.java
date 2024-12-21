package com.hammy275.immersivemc.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ButtonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ButtonBlock.class)
public interface ButtonBlockMixin {
    @Invoker("playSound")
    public void immersiveMC$playButtonSound(Player player, LevelAccessor levelAccessor, BlockPos blockPos, boolean bl);
}
