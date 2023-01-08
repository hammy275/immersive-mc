package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.subscribe.ClientLogicSubscriber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow protected int missTime;
    @Shadow public LocalPlayer player;

    @Inject(method="startUseItem", at=@At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true)
    public void preRightClick(CallbackInfo ci) {
        boolean doCancel = ClientLogicSubscriber.onClick(1);
        if (doCancel) {
            this.player.swing(InteractionHand.MAIN_HAND);
            ci.cancel();
        }
    }

    @Inject(method="startAttack", at=@At(value = "HEAD"), cancellable = true)
    public void preLeftClick(CallbackInfoReturnable<Boolean> cir) {
        boolean doCancel = ClientLogicSubscriber.onClick(0);
        if (doCancel) {
            this.missTime = ClientUtil.immersiveLeftClickCooldown;
            this.player.swing(InteractionHand.MAIN_HAND);
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

}
