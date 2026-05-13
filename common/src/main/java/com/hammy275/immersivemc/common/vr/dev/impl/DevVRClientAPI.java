package com.hammy275.immersivemc.common.vr.dev.impl;

import com.hammy275.immersivemc.common.vr.dev.DevVRState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.NotImplementedException;
import org.vivecraft.api.client.VRClientAPI;
import org.vivecraft.api.client.data.CloseKeyboardContext;
import org.vivecraft.api.client.data.OpenKeyboardContext;
import org.vivecraft.api.client.event.VivecraftClientRegistrationEvent;
import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.api.data.VRPoseHistory;

import java.util.function.Consumer;

public class DevVRClientAPI implements VRClientAPI {
    @Override
    public void addClientRegistrationHandler(Consumer<VivecraftClientRegistrationEvent> consumer) throws IllegalStateException {
        consumer.accept(new DevVivecraftClientRegistrationEvent());
    }

    @Override
    public boolean isVRInitialized() {
        return DevVRState.inVR;
    }

    @Override
    public boolean isVRActive() {
        return DevVRState.inVR;
    }

    @Override
    public VRPose getLatestRoomPose() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public VRPose getPostTickRoomPose() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public VRPose getPreTickWorldPose() {
        return DevVRState.pose;
    }

    @Override
    public VRPose getPostTickWorldPose() {
        return DevVRState.pose;
    }

    @Override
    public VRPose getWorldRenderPose() {
        return DevVRState.pose;
    }

    @Override
    public VRPoseHistory getHistoricalVRPoses() {
        return DevVRState.poseHistory;
    }

    @Override
    public void triggerHapticPulse(VRBodyPart vrBodyPart, float v, float v1, float v2, float v3) {
        Player player = Minecraft.getInstance().player;
        player.sendSystemMessage(Component.literal("Client haptic pulse for %s and body part %s".formatted(player.getGameProfile().name(), vrBodyPart)));
    }

    @Override
    public boolean isSeated() {
        return false;
    }

    @Override
    public boolean isLeftHanded() {
        return false;
    }

    @Override
    public FBTMode getFBTMode() {
        return FBTMode.ARMS_ONLY;
    }

    @Override
    public float getWorldScale() {
        return 1f;
    }

    @Override
    public boolean openKeyboard(OpenKeyboardContext openKeyboardContext) {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Opened VR keyboard"));
        return true;
    }

    @Override
    public boolean closeKeyboard(CloseKeyboardContext closeKeyboardContext) {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Closed VR keyboard"));
        return true;
    }
}
