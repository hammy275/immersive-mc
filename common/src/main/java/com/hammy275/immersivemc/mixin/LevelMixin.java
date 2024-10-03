package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.server.immersive.DirtyTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "blockEntityChanged", at = @At("RETURN"))
    private void blockEntityChanged(BlockPos blockPos, CallbackInfo ci) {
        Level me = (Level) (Object) this;
        BlockEntity blockEntity = me.getBlockEntity(blockPos);
        // Lecterns hold items, but aren't directly Containers
        if (!me.isClientSide && blockEntity instanceof Container || blockEntity instanceof LecternBlockEntity) {
            DirtyTracker.markDirty(me, blockPos);
        }
    }
}
