package com.hammy275.immersivemc.client.ticker;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ThrowPacket;
import com.hammy275.immersivemc.common.ticker.AbstractTicker;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.hammy275.immersivemc.common.vr.VRUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.VRAPI;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

public class ThrowTicker extends AbstractTicker {

    public static final ThrowTicker INSTANCE = new ThrowTicker();

    public int holdTime = 0;

    @Override
    protected void tick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        Item mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        if (Minecraft.getInstance().options.keyAttack.isDown() && Util.isThrowableItem(mainHandItem)) {
            holdTime++;
            ClientUtil.immersiveLeftClickCooldown = 6; // Prevent left clicking
        } else if (Util.isThrowableItem(mainHandItem)) {
            if (this.readyToThrow()) {
                Vec3 netMovement = VRUtil.changeForVelocity(Minecraft.getInstance().player, VRBodyPart.fromInteractionHand(InteractionHand.MAIN_HAND));
                if (netMovement == null) {
                    holdTime = 0;
                    return;
                }
                Vec3 throwDir = mainHandItem instanceof TridentItem ?
                        VRAPI.instance().getVRPose(player).getHand(InteractionHand.MAIN_HAND).getDir() :
                        netMovement.normalize();
                Network.INSTANCE.sendToServer(new ThrowPacket(netMovement, throwDir));
                VRRumble.rumbleIfVR(Minecraft.getInstance().player, InteractionHand.MAIN_HAND, CommonConstants.vibrationTimePlayerActionAlert);
            }
            holdTime = 0;
        }
    }

    @Override
    protected boolean shouldTick(Player player, VRPose pose, VRPoseHistory poseHistory) {
        return ActiveConfig.active().useThrowingImmersive;
    }

    public boolean readyToThrow() {
        return this.holdTime > 4;
    }
}
