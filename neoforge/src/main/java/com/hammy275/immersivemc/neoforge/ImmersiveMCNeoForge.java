package com.hammy275.immersivemc.neoforge;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.network.NetworkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCNeoForge {
    public ImmersiveMCNeoForge(IEventBus modBus) {
        Platform.COMMON = new PlatformCommonImpl();
        if (Platform.COMMON.isClient()) {
            Platform.CLIENT = new PlatformClientImpl();
        }
        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            PayloadRegistrar registrar = event.registrar(ImmersiveMC.MOD_ID);
            registrar.optional().playBidirectional(NetworkPacket.ID, NetworkPacket.CODEC,
                    (packet, ctx) ->
                            ctx.enqueueWork(() ->
                                    packet.handle((ServerPlayer) ctx.player())));
        });
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientSetup.doClientSetup(modBus);
        }
        ImmersiveMC.init();

        if (Platform.COMMON.isModLoaded("lootr")) {
            Lootr.lootrImpl = LootrCompatImpl.makeCompatImpl();
        }
    }
}
