package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {

    // Uses WrapMethod instead of ModifyReturnValue because mods (such as ImmersivePortals) tend to @Inject here
    @WrapMethod(method = "getOpenCount")
    private int immersiveMC$addImmersiveOpenersCount(Level level, BlockPos pos, Operation<Integer> original) {
        return original.call(level, pos) + ChestToOpenSet.getOpenCount(pos, level);
    }
}
