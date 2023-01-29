package com.hammy275.immersivemc.client.tracker;

import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.LastClientVRData;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ThrowPacket;
import com.hammy275.immersivemc.common.tracker.AbstractTracker;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

public class ThrowTracker extends AbstractTracker {

    public int holdTime = 0;

    public ThrowTracker() {
        ClientTrackerInit.trackers.add(this);
    }
    @Override
    protected void tick(Player player) {
        Item mainHandItem = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        if (Minecraft.getInstance().options.keyAttack.isDown() && Util.isThrowableItem(mainHandItem)) {
            holdTime++;
            ClientUtil.immersiveLeftClickCooldown = 2; // Prevent left clicking
        } else if (Util.isThrowableItem(mainHandItem)) {
            if (this.readyToThrow()) {
                Vec3 throwDir = mainHandItem instanceof TridentItem ?
                        LastClientVRData.getPlayer(0).getController(0).getLookAngle() :
                        LastClientVRData.changeForVelocity(LastClientVRData.VRType.C0).normalize();
                Network.INSTANCE.sendToServer(new ThrowPacket(
                        LastClientVRData.changeForVelocity(LastClientVRData.VRType.C0),
                        throwDir));
            }
            holdTime = 0;
        }
    }

    @Override
    protected boolean shouldTick(Player player) {
        return VRPluginVerify.clientInVR;
    }

    public boolean readyToThrow() {
        return this.holdTime > 4;
    }
}
