package com.hammy275.immersivemc.server.ticker.hand;

import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.ticker.AbstractHandTicker;
import com.hammy275.immersivemc.common.vr.VRRumble;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPoseHistory;

public class ArmorTicker extends AbstractHandTicker {

    @Override
    protected void tickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        boolean shouldEquip;
        ItemStack stackInHand = player.getItemInHand(hand);
        EquipmentSlot slot = player.getEquipmentSlotForItem(stackInHand);
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) return;
        if (!player.getInventory().getItem(slot.getIndex(36)).isEmpty()) return;
        switch (slot) {
            case HEAD:
                shouldEquip =
                        player.getEyePosition().distanceToSqr(handData.getPos()) <= 0.5*0.5;
                break;
            case CHEST:
                shouldEquip =
                        player.position().add(0, 1.2, 0).distanceToSqr(handData.getPos())
                                <= 0.5*0.5;
                break;
            case LEGS:
                shouldEquip =
                        player.position().add(0, 0.7, 0).distanceToSqr(handData.getPos())
                                <= 0.375*0.375;
                break;
            case FEET:
                shouldEquip = player.position().distanceToSqr(handData.getPos())
                        <= 0.5*0.5;
                break;
            default:
                return;
        }
        if (shouldEquip) {
            ItemStack toEquip = stackInHand.copy();
            toEquip.setCount(1);
            player.getInventory().setItem(slot.getIndex(36), toEquip);
            stackInHand.shrink(1);
            VRRumble.rumbleIfVR(player, hand, CommonConstants.vibrationTimePlayerActionAlert);
        }
    }

    @Override
    protected boolean shouldTickHand(Player player, InteractionHand hand, VRBodyPartData handData, VRPoseHistory poseHistory) {
        return ActiveConfig.getConfigForPlayer(player).useArmorImmersive && player.getBoundingBox().contains(handData.getPos());
    }
}
