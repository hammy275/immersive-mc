package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.ChestToOpenSet;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(value = ReachEntityAttributes.class)
public class ReachEntityAttributesMixin {

    @Inject(method = "getPlayersWithinReach(Ljava/util/function/Predicate;Lnet/minecraft/world/level/Level;IIID)Ljava/util/List;",
            at = @At("RETURN"))
    private static void immersiveMC$addPlayersImmersivelyOpening(Predicate<Player> isViewingChestPredicate, Level level, int x, int y, int z, double baseReachDistance, CallbackInfoReturnable<List<Player>> cir) {
        Set<Player> openSet = ChestToOpenSet.getOpenSet(level, new BlockPos(x, y, z), false);
        if (openSet != null) {
            List<Player> players = cir.getReturnValue();
            players.addAll(openSet.stream().filter(p -> !players.contains(p)).toList());
        }
    }
}
