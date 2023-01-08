package com.hammy275.immersivemc.common.network;

import dev.architectury.networking.NetworkChannel;
import com.hammy275.immersivemc.ImmersiveMC;
import net.minecraft.resources.ResourceLocation;

public class Network {

    private static final String FAKE_PROTOCOL_VERSION = ImmersiveMC.MOD_ID;

    public static final int PROTOCOL_VERSION = 2; // Increment post-release
    // Version compatability is handled during config syncing.

    public static final NetworkChannel INSTANCE = NetworkChannel.create(
            new ResourceLocation(ImmersiveMC.MOD_ID, "immersive_mc"));
}
