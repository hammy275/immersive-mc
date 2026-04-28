package com.hammy275.immersivemc.mixin;

import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnderChestBlockEntity.class)
public interface EnderChestBlockEntityAccessor {

    @Accessor("chestLidController")
    public ChestLidController immersiveMC$getChestLidController();

    @Accessor("openersCounter")
    ContainerOpenersCounter immersiveMC$openersCounter();
}
