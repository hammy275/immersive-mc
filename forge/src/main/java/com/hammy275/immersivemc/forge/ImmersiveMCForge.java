package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCForge {

    public static final SimpleChannel NETWORK = ChannelBuilder.named(Util.id("network"))
            .optional()
            .simpleChannel()
            .play().bidirectional().add(BufferPacket.class, BufferPacket.CODEC, (bufferPacket, context) -> {
                context.enqueueWork(() -> Network.INSTANCE.doReceive(context.getSender(), bufferPacket.buffer()));
                context.setPacketHandled(true);
            })
            .build();

    public ImmersiveMCForge() {
        Platform.COMMON = new PlatformCommonImpl();
        if (Platform.COMMON.isClient()) {
            Platform.CLIENT = new PlatformClientImpl();
        }
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
        ImmersiveMC.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.doClientSetup();
        }
    }


}
