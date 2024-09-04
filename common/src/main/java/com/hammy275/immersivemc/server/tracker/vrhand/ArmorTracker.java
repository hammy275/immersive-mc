package com.hammy275.immersivemc.server.tracker.vrhand;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.server.data.LastTickData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

public class ArmorTracker extends AbstractVRHandTracker {
    @Override
    protected boolean shouldRunForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        return player.getBoundingBox().contains(currentVRData.getController(hand.ordinal()).position()) &&
                stackInHand.getItem() instanceof Equipable;
    }

    @Override
    protected void runForHand(Player player, InteractionHand hand, ItemStack stackInHand, IVRPlayer currentVRData, LastTickData lastVRData) {
        boolean shouldEquip;
        EquipmentSlot slot = Mob.getEquipmentSlotForItem(stackInHand);
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) return;
        if (!player.getInventory().armor.get(slot.getIndex()).isEmpty()) return;
        switch (slot) {
            case HEAD:
                shouldEquip =
                        player.getEyePosition().distanceToSqr(currentVRData.getController(hand.ordinal()).position()) <= 0.5*0.5;
                break;
            case CHEST:
                shouldEquip =
                        player.position().add(0, 1.2, 0).distanceToSqr(currentVRData.getController(hand.ordinal()).position())
                        <= 0.5*0.5;
                break;
            case LEGS:
                shouldEquip =
                        player.position().add(0, 0.7, 0).distanceToSqr(currentVRData.getController(hand.ordinal()).position())
                                <= 0.375*0.375;
                break;
            case FEET:
                shouldEquip = player.position().distanceToSqr(currentVRData.getController(hand.ordinal()).position())
                        <= 0.5*0.5;
                break;
            default:
                return;
        }
        if (shouldEquip) {
            ItemStack toEquip = stackInHand.copy();
            toEquip.setCount(1);
            player.getInventory().armor.set(slot.getIndex(), toEquip);
            stackInHand.shrink(1);
            VRRumble.rumbleIfVR(player, hand.ordinal(), CommonConstants.vibrationTimePlayerActionAlert);
        }
    }

    @Override
    public boolean isEnabledInConfig(ActiveConfig config) {
        return config.useArmorImmersive;
    }
}
