package com.hammy275.immersivemc.common.vr.dev.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.ViveVersion;
import org.vivecraft.api.server.VRServerAPI;

public class DevVRServerAPI implements VRServerAPI {
    @Override
    public boolean hasVivecraft(ServerPlayer serverPlayer) {
        return true;
    }

    @Override
    public @Nullable ViveVersion getVivecraftVersion(ServerPlayer serverPlayer) {
        return VRServerAPI.instance().getVivecraftVersion(serverPlayer);
    }

    @Override
    public void sendHapticPulse(ServerPlayer serverPlayer, VRBodyPart vrBodyPart, float v, float v1, float v2, float v3) {
        serverPlayer.sendSystemMessage(Component.literal("Server haptic pulse for %s and body part %s".formatted(serverPlayer.getGameProfile().name(), vrBodyPart)));
    }
}
