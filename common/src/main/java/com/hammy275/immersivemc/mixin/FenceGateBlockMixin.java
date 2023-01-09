package com.hammy275.immersivemc.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.FenceGateBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FenceGateBlock.class)
public interface FenceGateBlockMixin {

    @Accessor("openSound")
    public SoundEvent getOpenSound();

    @Accessor("closeSound")
    public SoundEvent getCloseSound();
}
