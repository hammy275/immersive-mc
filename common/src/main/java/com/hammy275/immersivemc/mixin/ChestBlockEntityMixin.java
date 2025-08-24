package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.ClientMixinProxy;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin {

    @Accessor("chestLidController")
    public abstract ChestLidController immersiveMC$getChestLidController();

    @WrapMethod(method = "lidAnimateTick")
    private static void immersiveMC$wrapForControlledLid(Level level, BlockPos pos, BlockState state, ChestBlockEntity blockEntity, Operation<Void> original) {
        ClientMixinProxy.handleForcedLidAnimation(
                level,
                pos,
                state,
                blockEntity,
                (ChestLidControllerAccessor) ((ChestBlockEntityMixin) (Object) blockEntity).immersiveMC$getChestLidController(),
                original
        );
    }
}
