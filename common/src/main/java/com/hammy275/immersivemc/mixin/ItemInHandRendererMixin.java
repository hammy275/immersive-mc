package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.immersive_item.AbstractItemImmersive;
import com.hammy275.immersivemc.client.immersive_item.ItemImmersives;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
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

    @ModifyVariable(method = "renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                    at = @At("HEAD"), index = 6, ordinal = 0, argsOnly = true)
    private ItemStack overwriteItemStack(ItemStack stack, AbstractClientPlayer player, float f, float g, InteractionHand hand) {
        if (player == Minecraft.getInstance().player && VRPluginVerify.clientInVR()) {
            for (AbstractItemImmersive<?> immersive : ItemImmersives.ITEM_IMMERSIVES) {
                if (immersive.isEnabled() && immersive.itemMatches(stack)) {
                    return ItemStack.EMPTY; // ImmersiveMC handles rendering this item.
                }
            }
        }
        return stack;
    }
}
