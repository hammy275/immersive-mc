package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.network.Network;
import com.hammy275.immersivemc.common.network.NetworkPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ImmersiveMCFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(NetworkPacket.ID, NetworkPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(NetworkPacket.ID, NetworkPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(NetworkPacket.ID,
                ((payload, context) ->
                        context.server().execute(() -> payload.handle(context.player()))));
        if (Platform.isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(NetworkPacket.ID,
                    (payload, context) ->
                            context.client().execute(() -> payload.handle(null)));
            WorldRenderEvents.AFTER_ENTITIES.register(context ->
                    ClientRenderSubscriber.onWorldRender(context.matrices()));
        }
        ImmersiveMC.init();
        if (Platform.isModLoaded("lootr")) {
            Lootr.lootrImpl = LootrCompatImpl.makeCompatImpl();
        }
    }
}
