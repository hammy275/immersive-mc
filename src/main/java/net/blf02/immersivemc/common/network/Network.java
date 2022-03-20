package net.blf02.immersivemc.common.network;

import net.blf02.immersivemc.ImmersiveMC;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Network {

    private static final String PROTOCOL_VERSION = "1"; // Increment post-release whenever the network protocol changes

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ImmersiveMC.MOD_ID, "immersive_mc"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
}
