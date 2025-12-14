package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.immersive_item.AbstractHandImmersive;
import com.hammy275.immersivemc.client.immersive_item.HandImmersives;
import com.hammy275.immersivemc.common.vr.VRVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ItemInHandRenderer.class, priority = 998) // Priority 998 to come before Vivecraft's Inject
public class ItemInHandRendererMixin {

    @ModifyVariable(method = "renderArmWithItem",
                    at = @At("HEAD"), index = 6, ordinal = 0, argsOnly = true)
    private ItemStack immersiveMC$overwriteItemStack(ItemStack stack, AbstractClientPlayer player, float f, float g, InteractionHand hand) {
        if (player == Minecraft.getInstance().player && VRVerify.clientInVR()) {
            for (AbstractHandImmersive<?> immersive : HandImmersives.HAND_IMMERSIVES) {
                if (immersive.isEnabled() && immersive.activeForHand(hand)) {
                    return ItemStack.EMPTY; // ImmersiveMC handles rendering this item.
                }
            }
        }
        return stack;
    }
}
