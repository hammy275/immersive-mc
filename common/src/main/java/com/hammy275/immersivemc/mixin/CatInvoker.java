package com.hammy275.immersivemc.mixin;

import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.CatSoundVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Cat.class)
public interface CatInvoker {

    @Invoker("getSoundSet")
    public CatSoundVariant.CatSoundSet immersiveMC$getSoundSet();
}
