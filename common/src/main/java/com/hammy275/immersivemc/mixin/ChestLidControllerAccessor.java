package com.hammy275.immersivemc.mixin;

import net.minecraft.world.level.block.entity.ChestLidController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestLidController.class)
public interface ChestLidControllerAccessor {

    @Accessor("shouldBeOpen")
    public boolean getShouldBeOpen();
}
