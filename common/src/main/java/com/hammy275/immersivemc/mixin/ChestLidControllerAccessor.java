package com.hammy275.immersivemc.mixin;

import net.minecraft.world.level.block.entity.ChestLidController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestLidController.class)
public interface ChestLidControllerAccessor {

    @Accessor("openness")
    public void immersiveMC$setOpenness(float newOpenness);

    @Accessor("openness")
    public float immersiveMC$getOpenness();

    @Accessor("oOpenness")
    public void immersiveMC$setOldOpenness(float newOpenness);

    @Accessor("oOpenness")
    public float immersiveMC$getOldOpenness();
}
