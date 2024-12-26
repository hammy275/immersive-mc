package com.hammy275.immersivemc.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftMixinAccessor {

    @Accessor("rightClickDelay")
    public void immersiveMC$setRightClickDelay(int newAmount);

    @Accessor("rightClickDelay")
    public int immersiveMC$getRightClickDelay();




}
