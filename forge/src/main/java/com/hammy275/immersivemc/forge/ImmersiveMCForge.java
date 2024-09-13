package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.ImmersiveMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCForge {

    public static final SimpleChannel NETWORK = ChannelBuilder.named(new ResourceLocation(ImmersiveMC.MOD_ID, "network")).optional().simpleChannel();

    public ImmersiveMCForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
        NETWORK.messageBuilder(BufferPacket.class)
                .encoder(BufferPacket::encode)
                .decoder(BufferPacket::decode)
                .consumerNetworkThread(BufferPacket::handle)
                .add();
        ImmersiveMC.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.doClientSetup();
        }
    }


}
