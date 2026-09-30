package com.hammy275.immersivemc.mixin;

import com.hammy275.immersivemc.client.immersive_item.AbstractHandImmersive;
import com.hammy275.immersivemc.client.immersive_item.HandImmersives;
import com.hammy275.immersivemc.client.ticker.ThrowTicker;
import com.hammy275.immersivemc.common.vr.VRVerify;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = HumanoidMobRenderer.class, priority = 998) // Priority 998 to come before Vivecraft's Inject
public class HumanoidMobRendererMixin {

    @WrapMethod(method = "extractHumanoidRenderState")
    private static void immersiveMC$overwriteItemStack(LivingEntity entity, HumanoidRenderState state, float partialTicks, ItemModelResolver itemModelResolver, Operation<Void> original) {
        original.call(entity, state, partialTicks, itemModelResolver);
        if (entity == Minecraft.getInstance().player && VRVerify.clientInVR()) {
            boolean changed = false;
            for (AbstractHandImmersive<?> immersive : HandImmersives.HAND_IMMERSIVES) {
                for (InteractionHand hand : InteractionHand.values()) {
                    if (immersive.isEnabled() && immersive.activeForHand(hand)) {
                        if (state.mainArm == HumanoidArm.RIGHT) {
                            if (hand == InteractionHand.MAIN_HAND) {
                                state.rightHandItemStack = ItemStack.EMPTY;
                                state.rightHandItemState.clear();
                                changed = true;
                            } else {
                                state.leftHandItemStack = ItemStack.EMPTY;
                                state.leftHandItemState.clear();
                            }
                        } else {
                            if (hand == InteractionHand.MAIN_HAND) {
                                state.leftHandItemStack = ItemStack.EMPTY;
                                state.leftHandItemState.clear();
                                changed = true;
                            } else {
                                state.rightHandItemStack = ItemStack.EMPTY;
                                state.rightHandItemState.clear();
                            }
                        }
                    }
                }
            }
            if (!changed && entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TridentItem
                    && Minecraft.getInstance().options.keyAttack.isDown() && ThrowTicker.INSTANCE.readyToThrow()) {
                PlayerRenderState playerState = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.playerRenderState;
                state.useItemHand = InteractionHand.MAIN_HAND;
                playerState.firstPersonHandsAndItems.mainHandUseDuration = 72000;
                playerState.firstPersonHandsAndItems.useItemRemainingTicks = 72000 - 21;
            }
        }
    }
}
