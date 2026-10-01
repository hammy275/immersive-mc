package com.hammy275.immersivemc.fabric;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.client.subscribe.ClientRenderSubscriber;
import com.hammy275.immersivemc.common.compat.Lootr;
import com.hammy275.immersivemc.common.network.NetworkPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ImmersiveMCFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Platform.COMMON = new PlatformCommonImpl();
        if (Platform.COMMON.isClient()) {
            Platform.CLIENT = new PlatformClientImpl();
        }
        PayloadTypeRegistry.clientboundPlay().register(NetworkPacket.ID, NetworkPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(NetworkPacket.ID, NetworkPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(NetworkPacket.ID,
                ((payload, context) ->
                        context.server().execute(() -> payload.handle(context.player()))));
        if (Platform.COMMON.isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(NetworkPacket.ID,
                    (payload, context) ->
                            context.client().execute(() -> payload.handle(null)));
            LevelRenderEvents.COLLECT_SUBMITS.register(context ->
                    ClientRenderSubscriber.onWorldRender(context.poseStack()));
            LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(context ->
                    ClientRenderSubscriber.onTransparentRender(context.poseStack()));
        }
        ImmersiveMC.init();
        if (Platform.COMMON.isModLoaded("lootr")) {
            Lootr.lootrImpl = LootrCompatImpl.makeCompatImpl();
        }
    }
}
