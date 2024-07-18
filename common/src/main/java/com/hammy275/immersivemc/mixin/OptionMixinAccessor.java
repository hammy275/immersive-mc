package com.hammy275.immersivemc.mixin;

import net.minecraft.client.Option;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Option.class)
public interface OptionMixinAccessor {

    @Accessor("caption")
    public Component getCaption();
}
