package com.hammy275.immersivemc.mixin.throw_render_helpers.vivecraft;

import com.hammy275.immersivemc.client.immersive_item.AbstractHandImmersive;
import com.hammy275.immersivemc.client.immersive_item.HandImmersives;
import com.hammy275.immersivemc.client.ticker.ThrowTicker;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.vivecraft.client.extensions.FirstPersonHandsAndItemsStateExtension;
import org.vivecraft.client_vr.render.VivecraftItemRendering;
import org.vivecraft.client_vr.render.renderstates.FirstPersonHandsAdditions;

@Mixin(VivecraftItemRendering.class)
public class VivecraftItemRenderingMixin {

    @WrapMethod(method = "applyFirstPersonItemTransforms")
    private static void immersiveMC$renderReplacements(PoseStack poseStack, VivecraftItemRendering.VivecraftItemTransformType itemTransformType, boolean mainHand, PlayerRenderState playerState, float equippedProgress, float partialTick, ItemStack itemStack, InteractionHand hand, Operation<Void> original) {
        if (!playerState.hasPlayer) {
            original.call(poseStack, itemTransformType, mainHand, playerState, equippedProgress, partialTick, itemStack, hand);
        } else if (hand == InteractionHand.MAIN_HAND && itemStack.getItem() instanceof TridentItem
                && Minecraft.getInstance().options.keyAttack.isDown() && ThrowTicker.INSTANCE.readyToThrow()) {
            FirstPersonHandsAdditions playerAdditions = ((FirstPersonHandsAndItemsStateExtension)playerState.firstPersonHandsAndItems).vivecraft$getAdditions();
            playerAdditions.mainHandItemUseDuration = 72000;
            playerAdditions.useItemRemainingTicks = 72000 - 21;
            playerState.avatarRenderState.isUsingItem = true;
            playerState.avatarRenderState.useItemHand = InteractionHand.MAIN_HAND;
            original.call(poseStack, itemTransformType, mainHand, playerState, equippedProgress, partialTick, itemStack, hand);
        } else {
            for (AbstractHandImmersive<?> immersive : HandImmersives.HAND_IMMERSIVES) {
                if (immersive.isEnabled() && immersive.activeForHand(hand)) {
                    if (hand == InteractionHand.MAIN_HAND) {
                        playerState.firstPersonHandsAndItems.mainHandItem = ItemStack.EMPTY;
                        playerState.firstPersonHandsAndItems.mainHandRenderState.clear();
                    } else {
                        playerState.firstPersonHandsAndItems.offHandItem = ItemStack.EMPTY;
                        playerState.firstPersonHandsAndItems.offHandRenderState.clear();
                    }
                }
            }
            original.call(poseStack, itemTransformType, mainHand, playerState, equippedProgress, partialTick, itemStack, hand);
        }
    }
}
