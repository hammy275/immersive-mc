package net.blf02.immersivemc.mixin;

import net.blf02.immersivemc.server.ChestToOpenCount;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestTileEntity.class)
public class ChestTileEntityMixin {

    @Inject(method="getOpenCount(Lnet/minecraft/world/World;Lnet/minecraft/tileentity/LockableTileEntity;III)I",
    at=@At("RETURN"), cancellable = true)
    private static void onGetOpenCount(World world, LockableTileEntity chest, int ignored1, int ignored2, int ignored3, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() +
                ChestToOpenCount.chestImmersiveOpenCount.getOrDefault(chest.getBlockPos(), 0));
    }
}
