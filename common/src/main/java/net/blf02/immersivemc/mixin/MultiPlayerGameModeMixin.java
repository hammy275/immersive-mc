package net.blf02.immersivemc.mixin;

import net.blf02.immersivemc.common.config.ActiveConfig;
import net.blf02.immersivemc.common.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @ModifyVariable(method= "useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at=@At("STORE"), index = 9, ordinal = 1)
    // Matches bl2
    public boolean isCrouchingCondition(boolean value) {
        if (!ActiveConfig.crouchBypassImmersion) return value;
        HitResult rawResult = Minecraft.getInstance().hitResult;
        if (rawResult != null && rawResult instanceof BlockHitResult result) {
            if (Util.isHittingImmersive(result, Minecraft.getInstance().level)) {
                return false;
            }
        }
        return value;
    }
}
