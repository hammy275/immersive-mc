package com.hammy275.immersivemc.mixin;

import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LecternBlockEntity.class)
public interface LecternBlockEntityAccessor {

    @Invoker("setPage")
    public void immersiveMC$setPage(int page);
}
