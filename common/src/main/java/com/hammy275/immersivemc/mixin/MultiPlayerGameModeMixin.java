package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.ClientMixinProxy;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "performUseItemOn", at = @At("HEAD"))
    private void immersiveMC$performUseItemOnMaybeForceNoCrouching(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        if (ActiveConfig.getConfigForPlayer(player).crouchMode.bypassImmersive()) {
            ClientMixinProxy.pretendPlayerIsNotCrouching = true;
        }
    }

    @Inject(method = "performUseItemOn", at = @At("RETURN"))
    private void immersiveMC$performUseItemOnUndoForceNoCrouching(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        ClientMixinProxy.pretendPlayerIsNotCrouching = false;
    }
}
