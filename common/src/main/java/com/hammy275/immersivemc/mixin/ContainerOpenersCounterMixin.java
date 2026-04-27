package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.ClientMixinProxy;
import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
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
    private void immersiveMC$skipIncrementOpeners(Player player, Level level, BlockPos pos, BlockState state, Operation<Void> original) {
        if (!ClientMixinProxy.skipIncrementDecrementChests) {
            original.call(player, level, pos, state);
        } else {
            // Do a full re-check next tick (so ChestToOpenSet is up-to-date beforehand)
            level.scheduleTick(pos, state.getBlock(), 1);
        }
    }

    @WrapMethod(method = "decrementOpeners")
    private void immersiveMC$skipDecrementOpeners(Player player, Level level, BlockPos pos, BlockState state, Operation<Void> original) {
        if (!ClientMixinProxy.skipIncrementDecrementChests) {
            original.call(player, level, pos, state);
        } else {
            // Do a full re-check next tick (so ChestToOpenSet is up-to-date beforehand)
            level.scheduleTick(pos, state.getBlock(), 1);
        }
    }

    // Uses WrapMethod instead of ModifyReturnValue because mods (such as ImmersivePortals) tend to @Inject here
    @WrapMethod(method = "getPlayersWithContainerOpen")
    private List<Player> immersiveMC$addImmersiveOpenersCount(Level level, BlockPos pos, Operation<List<Player>> original) {
        List<Player> result = original.call(level, pos);
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
