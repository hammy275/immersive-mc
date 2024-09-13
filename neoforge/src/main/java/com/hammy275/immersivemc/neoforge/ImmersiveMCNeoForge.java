package com.hammy275.immersivemc.neoforge;

import com.hammy275.immersivemc.ImmersiveMC;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCNeoForge {

    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ImmersiveMC.MOD_ID, "network"))
            .networkProtocolVersion(() -> "3.0.10+")
            .serverAcceptedVersions(ignored -> true)
            .clientAcceptedVersions(ignored -> true).simpleChannel();

    public ImmersiveMCNeoForge(IEventBus modBus) {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.doClientSetup(modBus);
        }
        NETWORK.registerMessage(0, BufferPacket.class, BufferPacket::encode,
                BufferPacket::decode, BufferPacket::handle);
        ImmersiveMC.init();
    }
}
