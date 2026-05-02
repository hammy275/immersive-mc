package com.hammy275.immersivemc.client.interact_module;

import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.client.immersive.Immersives;
import com.hammy275.immersivemc.client.immersive.info.ChestInfo;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.ChestOpennessStorage;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.packet.ChestShulkerOpenPacket;
import com.hammy275.immersivemc.common.util.Util;
import com.hammy275.immersivemc.common.vr.VR;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.EnumMap;

public class ChestLidInteractModule implements HeldInteractModule {

    public static final ChestLidInteractModule INSTANCE = new ChestLidInteractModule();
    public static final double OPEN_THRESHOLD = 0.03;

    private static final ResourceLocation ID = Util.id("chest_lid");

    public final EnumMap<InteractionHand, ChestInfo> activeChests = new EnumMap<>(InteractionHand.class);


    @Override
    public void onRelease(LocalPlayer player, InteractionHand hand) {
        // On intentional release, open/close based on what we're closer to
        ChestInfo chestInfo = activeChests.get(hand);
        VRPoseHistory poseHistory = VR.ClientAPI.getHistoricalVRPoses();
        Vec3 last = poseHistory.getHistoricalData(1).getHand(hand).getPos();
        Vec3 current = VR.ClientAPI.getPreTickWorldPose().getHand(hand).getPos();
        double verticalDist = current.y - last.y;
        Network.INSTANCE.sendToServer(new ChestShulkerOpenPacket(chestInfo.getBlockPosition(), verticalDist >= OPEN_THRESHOLD));
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public boolean isActive(LocalPlayer player, InteractionHand hand, Vec3 handPos) {
        ChestInfo existingInfo = activeChests.get(hand);
        if (existingInfo != null) return false;
        // Can't get chest info based on block position since lid goes well outside of block bounds
        ChestInfo chestInfo = null;
        for (ChestInfo info : Immersives.immersiveChest.getTrackedObjects()) {
            for (BoundingBox box : info.openCloseHitboxes) {
                if (BoundingBox.contains(box, handPos)) {
                    chestInfo = info;
                    break;
                }
            }

            if (chestInfo != null) {
                break;
            }
        }
        if (chestInfo != null && chestInfo.getTicksExisted() >= 1) {
            boolean tookControl = chestInfo.takeControl(ChestOpennessStorage.AnimationState.PLAYER_CONTROLLED);
            if (tookControl) {
                activeChests.put(hand, chestInfo);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPress(LocalPlayer player, InteractionHand hand) {
        return true;
    }

    @Override
    public boolean onHoldTick(LocalPlayer player, InteractionHand hand) {
        ChestInfo info = activeChests.get(hand);
        if (!Immersives.immersiveChest.chestsValid(info) || !info.takeControl(ChestOpennessStorage.AnimationState.PLAYER_CONTROLLED)) {
            return false;
        }
        VRBodyPartData handData = VR.ClientAPI.getPreTickWorldPose().getHand(hand);
        Vec3 handPos = handData.getPos();
        Vec3 chestBottomCenter = Vec3.atBottomCenterOf(info.getBlockPosition());
        if (info.otherPos != null) {
            chestBottomCenter = chestBottomCenter.add(Vec3.atBottomCenterOf(info.otherPos)).scale(0.5);
        }
        Vec3 start = chestBottomCenter.add(0, 0.625, 0).add(info.forward.getUnitVec3().scale(0.5));
        Vec3 end = chestBottomCenter.add(0, 1.125, 0).add(info.forward.getOpposite().getUnitVec3().scale(0.5));
        double startToEndDist = start.distanceTo(end);


        Vec3 lineDir = end.subtract(start).normalize();
        Vec3 handVec = handPos.subtract(start);
        double distance = lineDir.dot(handVec);
        Vec3 progressCoordinate = start.add(lineDir.scale(distance));
        double handDistToEnd = progressCoordinate.distanceTo(end);
        double progress = (startToEndDist - handDistToEnd) / startToEndDist;

        info.setForcedOpenness((float) progress);
        info.syncOpennessToServerIfDirty();

        return true;
    }

    @Override
    public void reset(LocalPlayer player, InteractionHand hand) {
        activeChests.remove(hand);
    }

    public void removeInvalid() {
        for (InteractionHand hand : InteractionHand.values()) {
            ChestInfo info = activeChests.get(hand);
            if (info == null || !Immersives.immersiveChest.chestsValid(info)) {
                activeChests.remove(hand);
            }
        }
    }
}
