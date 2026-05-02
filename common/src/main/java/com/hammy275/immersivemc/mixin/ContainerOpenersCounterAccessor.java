package com.hammy275.immersivemc.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ContainerOpenersCounter.class)
public interface ContainerOpenersCounterAccessor {

    @Invoker("getEntitiesWithContainerOpen")
    List<ContainerUser> immersiveMC$getEntitiesWithContainerOpen(Level level, BlockPos pos);
}
