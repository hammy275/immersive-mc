package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Set;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {

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
