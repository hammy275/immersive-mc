package com.hammy275.immersivemc.mixin.throw_render_helpers.vivecraft;

import com.hammy275.immersivemc.client.tracker.ClientTrackerInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.render.VivecraftItemRendering;

@Mixin(VivecraftItemRendering.class)
public class VivecraftItemRenderingMixin {

    @Redirect(method = "applyFirstPersonItemTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/vivecraft/render/VivecraftItemRendering$VivecraftItemTransformType;ZLnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z"))
    private static boolean isUsingItemRedirect(AbstractClientPlayer player) {
        if (player == Minecraft.getInstance().player && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.TRIDENT
        && Minecraft.getInstance().options.keyAttack.isDown() && ClientTrackerInit.throwTracker.readyToThrow()) {
            return true;
        }
        return player.isUsingItem();
    }

    @Redirect(method = "applyFirstPersonItemTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/vivecraft/render/VivecraftItemRendering$VivecraftItemTransformType;ZLnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUseItemRemainingTicks()I"))
    private static int getUseItemRemainingTicks(AbstractClientPlayer player) {
        if (player == Minecraft.getInstance().player && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.TRIDENT
                && Minecraft.getInstance().options.keyAttack.isDown() && ClientTrackerInit.throwTracker.readyToThrow()) {
            return 72000 - 21;
        }
        return player.getUseItemRemainingTicks();
    }

    @Redirect(method = "applyFirstPersonItemTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/vivecraft/render/VivecraftItemRendering$VivecraftItemTransformType;ZLnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"))
    private static InteractionHand getUsedItemHandMixin(AbstractClientPlayer player) {
        if (player == Minecraft.getInstance().player && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.TRIDENT
                && Minecraft.getInstance().options.keyAttack.isDown() && ClientTrackerInit.throwTracker.readyToThrow()) {
            return InteractionHand.MAIN_HAND;
        }
        return player.getUsedItemHand();
    }
}
