package com.hammy275.immersivemc.neoforge;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.network.Network;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ImmersiveMC.MOD_ID)
public class ImmersiveMCNeoForge {
    public ImmersiveMCNeoForge(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            PayloadRegistrar registrar = event.registrar(ImmersiveMC.MOD_ID);
            registrar.optional().playBidirectional(BufferPacket.ID, BufferPacket.CODEC,
                    new DirectionalPayloadHandler<>(
                            (packet, ctx) -> ctx.enqueueWork(() -> Network.INSTANCE.doReceive(null, packet.buffer())),
                            (packet, ctx) -> ctx.enqueueWork(() -> Network.INSTANCE.doReceive((ServerPlayer) ctx.player(), packet.buffer()))
                    ));
        });
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.doClientSetup(modBus);
        }
        ImmersiveMC.init();

        if (Platform.isModLoaded("lootr")) {
            Lootr.lootrImpl = LootrCompatImpl.makeCompatImpl();
        }
    }
}
