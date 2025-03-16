package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.server.ServerMixinProxy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void immersiveMC$useItemOnMaybeForceNoCrouching(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (ActiveConfig.getConfigForPlayer(player).crouchMode.bypassImmersive()) {
            ServerMixinProxy.pretendPlayerIsNotCrouching = true;
        }
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void immersiveMC$useItemOnUndoForceNoCrouching(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        ServerMixinProxy.pretendPlayerIsNotCrouching = false;
    }
}
