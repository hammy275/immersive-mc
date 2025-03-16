package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.ChestToOpenSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(ContainerOpenersCounter.class)
public class ContainerOpenersCounterMixin {
    @Inject(method = "getPlayersWithContainerOpen", at = @At("RETURN"))
    private void immersiveMC$addImmersiveOpenersCount(Level level, BlockPos pos, CallbackInfoReturnable<List<Player>> cir) {
        Set<Player> immersivePlayers = ChestToOpenSet.getOpenSet(level, pos, false);
        if (immersivePlayers != null) {
            cir.getReturnValue().addAll(immersivePlayers);
        }
    }
}
