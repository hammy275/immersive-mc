package net.blf02.immersivemc.common.network;

import net.blf02.immersivemc.ImmersiveMC;

public class Network {

    private static final String FAKE_PROTOCOL_VERSION = ImmersiveMC.MOD_ID;

    public static final int PROTOCOL_VERSION = 3; // Increment post-release
    // Version compatability is handled during config syncing.

    public static final NetworkHandler INSTANCE = NetworkHandler.INSTANCE;


}
