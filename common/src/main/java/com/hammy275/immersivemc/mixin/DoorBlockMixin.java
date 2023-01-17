package com.hammy275.immersivemc.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.DoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DoorBlock.class)
public interface DoorBlockMixin {

    @Accessor("openSound")
    public SoundEvent openSound();

    @Accessor("closeSound")
    public SoundEvent closeSound();
}
