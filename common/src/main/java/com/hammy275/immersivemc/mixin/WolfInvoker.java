package com.hammy275.immersivemc.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Wolf.class)
public interface WolfInvoker {

    @Invoker("getSoundVariant")
    public Holder<WolfSoundVariant> immersiveMC$getSoundVariant();
}
