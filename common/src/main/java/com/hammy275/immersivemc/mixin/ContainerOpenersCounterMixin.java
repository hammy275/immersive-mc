package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.hammy275.immersivemc.server.ServerMixinProxy;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {

    @WrapMethod(method = "incrementOpeners")
    private void immersiveMC$skipIncrementOpeners(Player player, Level level, BlockPos pos, BlockState state, Operation<Void> original) {
        if (!ServerMixinProxy.skipIncrementDecrementChests || !(player instanceof ServerPlayer)) {
            original.call(player, level, pos, state);
        } else {
            ((ContainerOpenersCounter) (Object) this).recheckOpeners(level, pos, state);
        }
    }

    @WrapMethod(method = "decrementOpeners")
    private void immersiveMC$skipDecrementOpeners(Player player, Level level, BlockPos pos, BlockState state, Operation<Void> original) {
        if (!ServerMixinProxy.skipIncrementDecrementChests || !(player instanceof ServerPlayer)) {
            original.call(player, level, pos, state);
        } else {
            ((ContainerOpenersCounter) (Object) this).recheckOpeners(level, pos, state);
        }
    }

    // Uses WrapMethod instead of ModifyReturnValue because mods (such as ImmersivePortals) tend to @Inject here
    @WrapMethod(method = "getOpenCount")
    private int immersiveMC$addImmersiveOpenersCount(Level level, BlockPos pos, Operation<Integer> original) {
        return original.call(level, pos) + ChestToOpenSet.getOpenCount(pos, level);
    }
}
