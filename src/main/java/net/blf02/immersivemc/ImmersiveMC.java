package net.blf02.immersivemc;

import net.blf02.immersivemc.client.subscribe.ClientLogicSubscriber;
import net.blf02.immersivemc.client.subscribe.ClientRenderSubscriber;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMC {

    public static final String MOD_ID = "immersivemc";

    public ImmersiveMC() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    protected void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new ClientLogicSubscriber());
            MinecraftForge.EVENT_BUS.register(new ClientRenderSubscriber());
        });
    }

    protected void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            int index = 1;
            Network.INSTANCE.registerMessage(index++, SwapPacket.class, SwapPacket::encode,
                    SwapPacket::decode, SwapPacket::handle);
        });

    }
}
