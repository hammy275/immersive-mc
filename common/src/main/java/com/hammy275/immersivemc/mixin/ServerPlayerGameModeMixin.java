package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.server.PlayerConfigs;
import com.hammy275.immersivemc.server.ServerMixinData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Final
    @Shadow
    protected ServerPlayer player;

    @Inject(method= "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
    at=@At("HEAD"))
    public void headOfCrouchingCheck(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        ServerMixinData.results.put(serverPlayer.getUUID(), blockHitResult);
    }

    @ModifyVariable(method= "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
    at=@At("STORE"), index = 9, ordinal = 1)
    // Matches bl2
    public boolean isCrouchingCondition(boolean value) {
        if (!PlayerConfigs.getConfig(this.player).crouchBypassImmersion) return value;
        BlockHitResult result = ServerMixinData.results.get(this.player.getUUID());
        if (result != null) {
            if (Util.isHittingImmersive(result, this.player.level)) {
                return false;
            }
        }
        return value;
    }
}
