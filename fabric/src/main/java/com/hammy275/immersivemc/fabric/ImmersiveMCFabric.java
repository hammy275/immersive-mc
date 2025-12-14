package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ImmersiveMCFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(BufferPacket.ID, BufferPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BufferPacket.ID, BufferPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BufferPacket.ID, ((payload, context) -> {
            payload.buffer().retain();
            context.server().execute(() -> {
                try {
                    Network.INSTANCE.doReceive(context.player(), payload.buffer());
                } finally {
                    payload.buffer().release();
                }
            });
        }));
        if (Platform.isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(BufferPacket.ID, (payload, context) -> {
                payload.buffer().retain();
                context.client().execute(() -> {
                    try {
                        Network.INSTANCE.doReceive(null, payload.buffer());
                    } finally {
                        payload.buffer().release();
                    }
                });
            });
            WorldRenderEvents.AFTER_ENTITIES.register(context ->
                    ClientRenderSubscriber.onWorldRender(context.matrices()));
        }
        ImmersiveMC.init();
        if (Platform.isModLoaded("lootr")) {
            Lootr.lootrImpl = LootrCompatImpl.makeCompatImpl();
        }
    }
}
