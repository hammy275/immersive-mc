package net.blf02.immersivemc.common.network;

import net.blf02.immersivemc.ImmersiveMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Network {

    private static final String FAKE_PROTOCOL_VERSION = ImmersiveMC.MOD_ID;

    public static final int PROTOCOL_VERSION = 1; // Increment post-release
    // Version compatability is handled during config syncing.

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ImmersiveMC.MOD_ID, "immersive_mc"),
            () -> FAKE_PROTOCOL_VERSION,
            (ver) -> true,
            (ver) -> true
    );
}
