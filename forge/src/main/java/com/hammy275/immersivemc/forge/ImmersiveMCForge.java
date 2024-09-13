package com.hammy275.immersivemc.forge;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.compat.Lootr;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCForge {

    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(ImmersiveMC.MOD_ID, "network"))
            .networkProtocolVersion(() -> "3.0.10+")
            .serverAcceptedVersions(ignored -> true)
            .clientAcceptedVersions(ignored -> true).simpleChannel();

    public ImmersiveMCForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        NETWORK.registerMessage(0, BufferPacket.class,
                BufferPacket::encode, BufferPacket::decode, BufferPacket::handle);
        ImmersiveMC.init();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.doClientSetup();
        }
        if (Platform.isModLoaded("lootr")) {
            Lootr.lootrImpl = LootrCompatImpl.makeCompatImpl();
        }
    }


}
