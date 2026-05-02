package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.ClientMixinProxy;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Set;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {

    @WrapMethod(method = "incrementOpeners")
    private void immersiveMC$skipIncrementOpeners(LivingEntity entity, Level level, BlockPos pos, BlockState state, double maxInteractionRange, Operation<Void> original) {
        if (!ClientMixinProxy.skipIncrementDecrementChests) {
            original.call(entity, level, pos, state, maxInteractionRange);
        } else {
            ((ContainerOpenersCounter) (Object) this).recheckOpeners(level, pos, state);
        }
    }

    @WrapMethod(method = "decrementOpeners")
    private void immersiveMC$skipDecrementOpeners(LivingEntity entity, Level level, BlockPos pos, BlockState state, Operation<Void> original) {
        if (!ClientMixinProxy.skipIncrementDecrementChests) {
            original.call(entity, level, pos, state);
        } else {
            ((ContainerOpenersCounter) (Object) this).recheckOpeners(level, pos, state);
        }
    }

    // Uses WrapMethod instead of ModifyReturnValue because mods (such as ImmersivePortals) tend to @Inject here
    @WrapMethod(method = "getEntitiesWithContainerOpen")
    private List<ContainerUser> immersiveMC$addImmersiveOpenersCount(Level level, BlockPos pos, Operation<List<ContainerUser>> original) {
        List<ContainerUser> result = original.call(level, pos);
        Set<Player> immersivePlayers = ChestToOpenSet.getOpenSet(level, pos, false);
        if (immersivePlayers != null) {
            for (Player immersivePlayer : immersivePlayers) {
                if (!result.contains(immersivePlayer)) {
                    result.add(immersivePlayer);
                }
            }
        }
        return result;
    }
}
