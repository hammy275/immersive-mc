package com.hammy275.immersivemc.mixin;

import net.minecraft.world.level.block.DoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DoorBlock.class)
public interface DoorBlockMixin {

    @Invoker("getOpenSound")
    public int openSound();

    @Invoker("getCloseSound")
    public int closeSound();
}
